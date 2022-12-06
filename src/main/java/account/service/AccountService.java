package account.service;

import account.dto.*;
import account.entity.Account;
import account.entity.PaymentDetails;
import account.entity.Role;
import account.exception.*;
import account.repository.AccountRepository;
import account.repository.BreachedPasswordRepository;
import account.repository.PaymentRepository;
import account.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final BreachedPasswordRepository breachedPasswordRepository;
    private final PaymentRepository paymentRepository;
    private final RoleRepository roleRepository;
    public static final int MAX_FAILED_ATTEMPTS = 5;
    private static final long LOCK_TIME_DURATION = 24 * 60 * 60 * 1000;

    @Autowired
     public AccountService(
             AccountRepository accountRepository,
             BCryptPasswordEncoder passwordEncoder,
             BreachedPasswordRepository breachedPasswordRepository,
             PaymentRepository paymentRepository,
             RoleRepository roleRepository) {

        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.breachedPasswordRepository = breachedPasswordRepository;
        this.paymentRepository = paymentRepository;
        this.roleRepository = roleRepository;
    }

    public Account signup(Account account) {

        if (breachedPasswordRepository.containsPassword(account.getPassword())) {
            throw new AccountPasswordException("The password is in the hacker's database!");

        } else if (accountRepository.existsByEmail(account.getEmail().toLowerCase(Locale.ROOT))) {
            throw new AccountExistsException();
        }

        Role userRole;

        if (isFirstUser()) {

            userRole = getAccountRole("ROLE_ADMINISTRATOR");
        } else {

            userRole = getAccountRole("ROLE_USER");
        }

        account.addRole(userRole);
        account.setPassword(passwordEncoder.encode(account.getPassword()));
        account.setEmail(account.getEmail().toLowerCase(Locale.ROOT));
        account.setAccountNonLocked(true);

        return accountRepository.save(account);
    }

    public ChangePasswordResponse changePassword(String currentUserEmail, NewPassword password) {

        Account account = accountRepository.findByEmail(currentUserEmail).get();
        String encryptedCurrentPassword = account.getPassword();

        if (passwordEncoder.matches(password.getPassword(), encryptedCurrentPassword)){
            throw new AccountPasswordException("The passwords must be different!");
        }

        if (breachedPasswordRepository.containsPassword(password.getPassword())) {
            throw new AccountPasswordException("The password is in the hacker's database!");
        }

        account.setPassword(passwordEncoder.encode(password.getPassword()));
        accountRepository.save(account);

        return new ChangePasswordResponse(account.getEmail(), "The password has been updated successfully");
    }

    @Transactional
    public PaymentResponse transactionalAddPaymentDetails(List<PaymentDetails> paymentDetailsList) {

        for (PaymentDetails payment : paymentDetailsList) {

            Account account = getAccountByEmail(payment.getEmployee().toLowerCase(Locale.ROOT));

            if (getPaymentDetailsByPeriod(account, payment.getPeriod()).isPresent()) {

                throw new PeriodExistsException(payment.getPeriod() + "," + account.getEmail());

            } else {

                payment.setAccount(account);
                paymentRepository.save(payment);
            }
        }

        return new PaymentResponse("Added successfully!");
    }

    public PaymentResponse updatePaymentDetails(PaymentDetails payment)
            throws PaymentNotExistsException {

        Account account = getAccountByEmail(payment.getEmployee());
        Optional<PaymentDetails> paymentDetails = getPaymentDetailsByPeriod(account, payment.getPeriod());

        if (paymentDetails.isEmpty()) {

            throw new PaymentNotExistsException();
        }

        paymentDetails.get().setSalary(payment.getSalary());
        accountRepository.save(account);

        return new PaymentResponse("Updated successfully!");
    }

    public AccountPaymentDetails getEmployeePaymentDetailsByPeriod(String currentUserEmail, String period)
            throws PaymentNotExistsException {

        Account account = getAccountByEmail(currentUserEmail);
        Optional<PaymentDetails> paymentDetails = getPaymentDetailsByPeriod(account, period);

        if (paymentDetails.isEmpty()) {

            throw new PaymentNotExistsException();
        }

        return new AccountPaymentDetails(
                account.getName(),
                account.getLastname(),
                parseToYearMonth(period),
                paymentDetails.get().getSalary()
        );
    }

    public List<Account> getAllUsersSortedById() {

        return accountRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(Account::getId))
                .collect(Collectors.toList());
    }

    public List<AccountPaymentDetails> getAllEmployeePayments(String currentUserEmail) {

        Account account = getAccountByEmail(currentUserEmail);

        return account.getSalaryDetailsList().stream()
                .map(paymentDetails -> parseToAccountPaymentDetails(
                        account.getName(),
                        account.getLastname(),
                        paymentDetails
                ))
                .sorted(Comparator.comparing(AccountPaymentDetails::getYearMonthPeriod).reversed())
                .collect(Collectors.toList());
    }

    public Account changeUserRole(ChangeRole role) {

        Account account = getAccountByEmail(role.getUser());

        if (role.getOperation().equals("GRANT")) {

            return grantRole(account, role.getRole());

        } else {

            return removeRole(account, role.getRole());
        }
    }

    public AccountResponse deleteAccount(String email) {

        Account account = getAccountByEmail(email);

        if (isAdministrator(account.getRoles())) {

            throw new RoleException("Can't remove ADMINISTRATOR role!");
        }

        accountRepository.delete(account);

        return new AccountResponse(email, "Deleted successfully!");
    }

    public void increaseFailedAttempt(Account account) {

        int newFailedAttempt = account.getFailedAttempt() + 1;
        accountRepository.updateFailedAttempts(newFailedAttempt, account.getEmail());
    }

    public void resetFailedAttempt(String email) {

        accountRepository.updateFailedAttempts(0, email);
    }

    public void lockAccount(Account account) {

        account.setAccountNonLocked(false);
        account.setLockTime(new Date());

        accountRepository.save(account);
    }

    public boolean unlockWhenTimeExpired(Account account) {
        long lockTimeInMillis = account.getLockTime().getTime();
        long currentTimeInMillis = System.currentTimeMillis();

        if (lockTimeInMillis + LOCK_TIME_DURATION < currentTimeInMillis) {
            account.setAccountNonLocked(true);
            account.setLockTime(null);
            account.setFailedAttempt(0);

            accountRepository.save(account);

            return true;
        }

        return false;
    }

    private Account grantRole(Account account, String grantedRole) {
        List<String> currentRoles = account.getRoles();

        if (currentRoles.contains(grantedRole)) {

            throw new RoleException("Role already granted");

        } else if (isAdministrator(currentRoles) ||
                grantedRole.equals("ROLE_ADMINISTRATOR")) {

            throw new RoleException("The user cannot combine administrative and business roles!");
        }


        Role role = getAccountRole(grantedRole);
        account.addRole(role);

        return accountRepository.save(account);
    }

    private Account removeRole(Account account, String roleToRemove) {
        List<String> currentRoles = account.getRoles();

        if (isAdministrator(currentRoles) &&
                roleToRemove.equals("ROLE_ADMINISTRATOR")) {

            throw new RoleException("Can't remove ADMINISTRATOR role!");

        } else if (!currentRoles.contains(roleToRemove)) {

            throw new RoleException("The user does not have a role!");

        } else if (currentRoles.size() == 1) {

            throw new RoleException("The user must have at least one role!");

        }

        Role role = getAccountRole(roleToRemove);
        account.removeRole(role);

        return accountRepository.save(account);
    }

    private boolean isFirstUser() {
        return accountRepository.findAll().isEmpty();
    }

    private boolean isAdministrator(List<String> roles) {

        return roles.contains("ROLE_ADMINISTRATOR");
    }

    private AccountPaymentDetails parseToAccountPaymentDetails(String name, String lastName, PaymentDetails payment) {

        return new AccountPaymentDetails(
                name,
                lastName,
                parseToYearMonth(payment.getPeriod()),
                payment.getSalary()
        );
    }

    private YearMonth parseToYearMonth(String period) {

        return YearMonth.parse(period, DateTimeFormatter.ofPattern("MM-yyyy"));
    }

    private Optional<PaymentDetails> getPaymentDetailsByPeriod(Account account, String period) {

        return account.getSalaryDetailsList().stream()
                .filter(paymentEntry -> paymentEntry.getPeriod().equals(period))
                .findFirst();

    }

    public Account getAccountByEmail(String email) throws AccountNotExistsException {

        Optional<Account> optionalAccount = accountRepository.findByEmail(email.toLowerCase(Locale.ROOT));

        if (optionalAccount.isPresent()) {

            return  optionalAccount.get();

        } else {

            throw new AccountNotExistsException();
        }
    }

    private Role getAccountRole(String role) throws RoleNotExistsException {

        Optional<Role> optionalRole = roleRepository.findByCode(role);

        if (optionalRole.isPresent()) {

            return optionalRole.get();

        } else {

            throw new RoleNotExistsException();
        }
    }

}
