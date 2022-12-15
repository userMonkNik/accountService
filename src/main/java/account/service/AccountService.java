package account.service;

import account.dto.*;
import account.entity.Account;
import account.entity.PaymentDetails;
import account.entity.Role;
import account.entity.SecurityLogs;
import account.exception.*;
import account.repository.AccountRepository;
import account.repository.BreachedPasswordRepository;
import account.repository.PaymentRepository;
import account.repository.RoleRepository;
import account.util.Action;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
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
    private final SecurityLogsService logService;
    public static final int MAX_FAILED_ATTEMPTS = 5;
    private static final long LOCK_TIME_DURATION = 24 * 60 * 60 * 1000;

    @Autowired
     public AccountService(
            AccountRepository accountRepository,
            BCryptPasswordEncoder passwordEncoder,
            BreachedPasswordRepository breachedPasswordRepository,
            PaymentRepository paymentRepository,
            RoleRepository roleRepository,
            SecurityLogsService logService) {

        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.breachedPasswordRepository = breachedPasswordRepository;
        this.paymentRepository = paymentRepository;
        this.roleRepository = roleRepository;
        this.logService = logService;
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


        logService.log(
                LocalDateTime.now(),
                Action.CREATE_USER,
                "Anonymous",
                account.getEmail()
        );

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

        logService.log(
                LocalDateTime.now(),
                Action.CHANGE_PASSWORD,
                account.getEmail(),
                account.getEmail()
        );

        return new ChangePasswordResponse(account.getEmail(), "The password has been updated successfully");
    }

    @Transactional
    public StatusResponse transactionalAddPaymentDetails(List<PaymentDetails> paymentDetailsList) {

        for (PaymentDetails payment : paymentDetailsList) {

            Account account = getAccountByEmail(payment.getEmployee().toLowerCase(Locale.ROOT));

            if (getPaymentDetailsByPeriod(account, payment.getPeriod()).isPresent()) {

                throw new PeriodExistsException(payment.getPeriod() + "," + account.getEmail());

            } else {

                payment.setAccount(account);
                paymentRepository.save(payment);
            }
        }

        return new StatusResponse("Added successfully!");
    }

    public StatusResponse updatePaymentDetails(PaymentDetails payment)
            throws PaymentNotExistsException {

        Account account = getAccountByEmail(payment.getEmployee());
        Optional<PaymentDetails> paymentDetails = getPaymentDetailsByPeriod(account, payment.getPeriod());

        if (paymentDetails.isEmpty()) {

            throw new PaymentNotExistsException();
        }

        paymentDetails.get().setSalary(payment.getSalary());
        accountRepository.save(account);

        return new StatusResponse("Updated successfully!");
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

    public Account changeUserRole(ChangeRole role, String adminEmail) {

        Account account = getAccountByEmail(role.getUser());

        if (role.getOperation().equals("GRANT")) {

            return grantRole(account, role.getRole(), adminEmail);

        } else {

            return removeRole(account, role.getRole(), adminEmail);
        }
    }

    public AccountResponse deleteAccount(String email, String adminEmail) {

        Account account = getAccountByEmail(email);

        if (isAdministrator(account.getRoles())) {

            throw new RoleException("Can't remove ADMINISTRATOR role!");
        }

        accountRepository.delete(account);

        logService.log(
                LocalDateTime.now(),
                Action.DELETE_USER,
                adminEmail,
                account.getEmail()
        );

        return new AccountResponse(email, "Deleted successfully!");
    }

    public void increaseFailedAttempt(Account account) {

        int newFailedAttempt = account.getFailedAttempt() + 1;
        accountRepository.updateFailedAttempts(newFailedAttempt, account.getEmail());
    }

    public void resetFailedAttempt(String email) {

        accountRepository.updateFailedAttempts(0, email);
    }

    public StatusResponse lockAccount(Account account, String adminEmail) {

        if (!account.isAccountNonLocked()) {
            throw new LockException("Account already blocked.");

        } else if (isAdministrator(account.getRoles())) {
            throw new LockException("Can't lock the ADMINISTRATOR!");
        }

        account.setAccountNonLocked(false);
        account.setLockTime(new Date());
        accountRepository.save(account);

        logService.log(
                LocalDateTime.now(),
                Action.LOCK_USER,
                adminEmail,
                "Lock user " + account.getEmail()
        );

        return new StatusResponse(String.format(
                "User %s locked!", account.getEmail()
        ));
    }

    private StatusResponse unlockAccount(Account account, String adminEmail) {

        if (account.isAccountNonLocked()) {
            throw new LockException("Account already unlocked");
        }

        account.setAccountNonLocked(true);
        account.setLockTime(null);
        account.setFailedAttempt(0);
        accountRepository.save(account);

        logService.log(
                LocalDateTime.now(),
                Action.UNLOCK_USER,
                adminEmail,
                "Unlock user " + account.getEmail()
        );

        return new StatusResponse(String.format(
                "User %s unlocked!", account.getEmail()
        ));
    }

    public boolean unlockWhenTimeExpired(Account account) {
        long lockTimeInMillis = account.getLockTime().getTime();
        long currentTimeInMillis = System.currentTimeMillis();

        if (lockTimeInMillis + LOCK_TIME_DURATION < currentTimeInMillis) {
            account.setAccountNonLocked(true);
            account.setLockTime(null);
            account.setFailedAttempt(0);

            accountRepository.save(account);

            logService.log(
                    LocalDateTime.now(),
                    Action.UNLOCK_USER,
                    "Server",
                    "Unlock user " + account.getEmail()
            );

            return true;
        }

        return false;
    }

    public StatusResponse putUserAccess(ChangeAccess changeAccess, String adminEmail) throws LockException {
        Account account = getAccountByEmail(changeAccess.getUser());

        switch (changeAccess.getOperation()) {
            case "LOCK" :
                return lockAccount(account, adminEmail);
            case "UNLOCK" :
                return unlockAccount(account, adminEmail);
            default :
                return null;
        }
    }

    private Account grantRole(Account account, String grantedRole, String adminEmail) {
        List<String> currentRoles = account.getRoles();

        if (currentRoles.contains(grantedRole)) {

            throw new RoleException("Role already granted");

        } else if (isAdministrator(currentRoles) ||
                grantedRole.equals("ROLE_ADMINISTRATOR")) {

            throw new RoleException("The user cannot combine administrative and business roles!");
        }


        Role role = getAccountRole(grantedRole);
        account.addRole(role);

        logService.log(
                LocalDateTime.now(),
                Action.GRANT_ROLE,
                adminEmail,
                String.format("Grant role %s to %s", grantedRole.substring(5), account.getEmail())
        );

        return accountRepository.save(account);
    }

    private Account removeRole(Account account, String roleToRemove, String adminEmail) {
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

        logService.log(
                LocalDateTime.now(),
                Action.REMOVE_ROLE,
                adminEmail,
                String.format("Remove role %s to %s", roleToRemove.substring(5), account.getEmail())
        );

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
