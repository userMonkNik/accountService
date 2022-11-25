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
import org.springframework.web.server.ResponseStatusException;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final BreachedPasswordRepository breachedPasswordRepository;
    private final PaymentRepository paymentRepository;
    private final RoleRepository roleRepository;

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

        Role userRole = roleRepository.findByName("User");

        account.setPassword(passwordEncoder.encode(account.getPassword()));
        account.setRoles(List.of(userRole));
        account.setEmail(account.getEmail().toLowerCase(Locale.ROOT));

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
            throws PaymentNotExistException {

        Account account = getAccountByEmail(payment.getEmployee());
        Optional<PaymentDetails> paymentDetails = getPaymentDetailsByPeriod(account, payment.getPeriod());

        if (paymentDetails.isEmpty()) {

            throw new PaymentNotExistException();
        }

        paymentDetails.get().setSalary(payment.getSalary());
        accountRepository.save(account);

        return new PaymentResponse("Updated successfully!");
    }

    public AccountPaymentDetails getEmployeePaymentDetailsByPeriod(String currentUserEmail, String period)
            throws PaymentNotExistException{

        Account account = getAccountByEmail(currentUserEmail);
        Optional<PaymentDetails> paymentDetails = getPaymentDetailsByPeriod(account, period);

        if (paymentDetails.isEmpty()) {

            throw new PaymentNotExistException();
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

    public AccountResponse deleteAccount(String email) {

        Account account = getAccountByEmail(email);

        if (isAdministrator(account)) {

            throw new RoleException("Can't remove ADMINISTRATOR role!");
        }

        accountRepository.delete(account);

        return new AccountResponse(email, "Deleted successfully!");
    }

    private boolean isAdministrator(Account account) {

        return account.getRoles().contains("ADMINISTRATOR");
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

    private Account getAccountByEmail(String email) throws AccountNotExistsException {

        Optional<Account> account = accountRepository.findByEmail(email.toLowerCase(Locale.ROOT));

        if (account.isPresent()) {

            return  account.get();
        } else {

            throw new AccountNotExistsException();
        }
    }

}
