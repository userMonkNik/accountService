package accountService.service;

import accountService.dto.ChangePasswordResponse;
import accountService.dto.NewPassword;
import accountService.entity.Account;
import accountService.exception.AccountExistsException;
import accountService.exception.AccountPasswordException;
import accountService.repository.AccountRepository;
import accountService.repository.BreachedPasswordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final BreachedPasswordRepository breachedPasswordRepository;

    @Autowired
     public AccountService(
             AccountRepository accountRepository,
             BCryptPasswordEncoder passwordEncoder,
             BreachedPasswordRepository breachedPasswordRepository) {

        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.breachedPasswordRepository = breachedPasswordRepository;
    }

    public Account signup(Account account) {

        if (accountRepository.existsByEmail(account.getEmail().toLowerCase(Locale.ROOT))) {
            throw new AccountExistsException();

        } else if (breachedPasswordRepository.containsPassword(account.getPassword())) {
            throw new AccountPasswordException("The password is in the hacker's database!");
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

    public Account getCurrentUser(String email) {
        return accountRepository.findByEmail(email).get();
    }

}
