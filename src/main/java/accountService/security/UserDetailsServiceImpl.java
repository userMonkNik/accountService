package accountService.security;

import accountService.entity.Account;
import accountService.exception.AccountNotExistsException;
import accountService.repository.AccountRepository;
import accountService.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Optional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private final AccountRepository repository;

    @Autowired
    public UserDetailsServiceImpl(AccountRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<Account> account = repository.findByEmail(email.toLowerCase(Locale.ROOT));

        if (account.isPresent()) {

            return new UserDetailsImpl(account.get());
        }

        throw new AccountNotExistsException();
    }
}
