package account.service;

import account.dto.AccountPaymentDetails;
import account.dto.PaymentResponse;
import account.dto.ChangePasswordResponse;
import account.dto.NewPassword;
import account.entity.Account;
import account.entity.PaymentDetails;
import account.exception.*;
import account.repository.AccountRepository;
import account.repository.BreachedPasswordRepository;
import account.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.web.servlet.oauth2.resourceserver.OpaqueTokenDsl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final BreachedPasswordRepository breachedPasswordRepository;
    private final PaymentRepository paymentRepository;

    @Autowired
     public AccountService(
             AccountRepository accountRepository,
             BCryptPasswordEncoder passwordEncoder,
             BreachedPasswordRepository breachedPasswordRepository,
             PaymentRepository paymentRepository) {

        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.breachedPasswordRepository = breachedPasswordRepository;
        this.paymentRepository = paymentRepository;
    }

    public Account signup(Account account) {

        if (breachedPasswordRepository.containsPassword(account.getPassword())) {
            throw new AccountPasswordException("The password is in the hacker's database!");

        } else if (accountRepository.existsByEmail(account.getEmail().toLowerCase(Locale.ROOT))) {
            throw new AccountExistsException();
        }

        account.setPassword(passwordEncoder.encode(account.getPassword()));
        account.setGrantedAuthority("ROLE_USER");
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

    public PaymentResponse updatePaymentDetails(PaymentDetails payment) {

        Account account = getAccountByEmail(payment.getEmployee());
        Optional<PaymentDetails> paymentDetails = getPaymentDetailsByPeriod(account, payment.getPeriod());

        if (paymentDetails.isEmpty()) {

            throw new PaymentNotExistException();
        }

        paymentDetails.get().setSalary(payment.getSalary());
        accountRepository.save(account);

        return new PaymentResponse("Updated successfully!");
    }

    public AccountPaymentDetails getEmployeePaymentDetailsByPeriod(String currentUserEmail, String period) {

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

    private YearMonth parseToYearMonth(String period) {

        return YearMonth.parse(period, DateTimeFormatter.ofPattern("MM-yyyy"));
    }

    private Optional<PaymentDetails> getPaymentDetailsByPeriod(Account account, String period) {

        return account.getSalaryDetailsList().stream()
                .filter(paymentEntry -> paymentEntry.getPeriod().equals(period))
                .findFirst();

    }

    private Account getAccountByEmail(String email) {

        Optional<Account> account = accountRepository.findByEmail(email.toLowerCase(Locale.ROOT));

        if (account.isPresent()) {

            return  account.get();
        } else {

            throw new AccountNotExistsException();
        }
    }

}
