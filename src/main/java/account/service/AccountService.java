package account.service;

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

        return new ChangePasswordResponse(currentUserEmail, "The password has been updated successfully");
    }

    @Transactional
    public PaymentResponse transactionalAddPaymentDetails(List<PaymentDetails> paymentDetailsList) {

        for (PaymentDetails payment : paymentDetailsList) {

            Account account = getAccountByEmail(payment.getEmployee());

            if (getPaymentDetailsByPeriod(account, payment).isPresent()) {

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
        Optional<PaymentDetails> paymentDetails = getPaymentDetailsByPeriod(account, payment);

        if (paymentDetails.isEmpty()) {

            throw new PaymentNotExistException();
        }

        paymentDetails.get().setSalary(payment.getSalary());
        accountRepository.save(account);

        return new PaymentResponse("Updated successfully!");
    }

    private Optional<PaymentDetails> getPaymentDetailsByPeriod(Account account, PaymentDetails payment) {

        return account.getSalaryDetailsList().stream()
                .filter(paymentEntry -> paymentEntry.getPeriod().equals(payment.getPeriod()))
                .findFirst();

    }

    private Account getAccountByEmail(String email) {

        Optional<Account> account = accountRepository.findByEmail(email);

        if (account.isPresent()) {

            return  account.get();
        } else {

            throw new AccountNotExistsException();
        }
    }

}
