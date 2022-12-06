package account.security;

import account.entity.Account;
import account.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

@Component
public class SuccessfullAuthenticationListener {

    @Autowired
    private AccountService service;

    @EventListener
    public void onApplicationEvent(AuthenticationSuccessEvent event) {

        Account account = service.getAccountByEmail(event.getAuthentication().getName());
        service.resetFailedAttempt(account.getEmail());
    }
}
