package account.security;

import account.entity.Account;
import account.service.AccountService;
import account.service.SecurityLogsService;
import account.util.Action;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Component
public class AuthenticationListener {

    @Autowired
    private AccountService accountService;
    @Autowired
    private SecurityLogsService logService;

    @Autowired
    private HttpServletRequest request;


    @EventListener
    public void onSuccess(AuthenticationSuccessEvent event) {

        Account account = accountService.getAccountByEmail(event.getAuthentication().getName());
        accountService.resetFailedAttempt(account.getEmail());
    }

    @EventListener
    public void onFailure(AbstractAuthenticationFailureEvent event) {

        logService.log(
                LocalDateTime.now(),
                Action.LOGIN_FAILED,
                event.getAuthentication().getName(),
                request.getRequestURI()
        );
    }
}
