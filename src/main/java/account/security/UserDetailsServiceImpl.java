package account.security;

import account.entity.Account;
import account.exception.AccountNotExistsException;
import account.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.*;

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

        if (account.isPresent() && account.get().isAccountNonLocked()) {

            return new UserDetailsImpl(
                    account.get().getEmail(),
                    account.get().getPassword(),
                    parseToGrantedAuthority(account.get().getRoles()),
                    account.get().isAccountNonLocked()
            );

        }

        throw new AccountNotExistsException();
    }

    private Collection<? extends GrantedAuthority> parseToGrantedAuthority(List<String> accountRoles) {
        Collection<SimpleGrantedAuthority> authoritiesList = new ArrayList<>(accountRoles.size());

        for (String role : accountRoles) {

            authoritiesList.add(new SimpleGrantedAuthority(role));
        }

        return authoritiesList;
    }
}
