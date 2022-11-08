package accountService.service;

import accountService.entity.Account;
import accountService.exception.AccountExistsException;
import accountService.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
     public AccountService(AccountRepository accountRepository, BCryptPasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Account signup(Account account) {

        if (accountRepository.existsByEmail(account.getEmail().toLowerCase(Locale.ROOT))) {
            throw new AccountExistsException();
        }

        return register(account);
    }

    private Account register(Account account) {

        account.setPassword(passwordEncoder.encode(account.getPassword()));
        account.setGrantedAuthority("ROLE_USER");
        account.setEmail(account.getEmail().toLowerCase(Locale.ROOT));

        return accountRepository.save(account);
    }

    public Account getCurrentUser(String email) {
        return accountRepository.findByEmail(email).get();
    }
}
