package account.security;

import account.entity.Account;
import account.service.AccountService;
import account.service.SecurityLogsService;
import account.util.Action;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Autowired
    private AccountService accountService;
    @Autowired
    private SecurityLogsService logService;
    private final ObjectMapper mapper = new ObjectMapper();


    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException)
            throws IOException, ServletException {

        String email = getLoginFromAuthorization(request.getHeader("authorization"));

        Account account = null;
        try {
            account = accountService.getAccountByEmail(email);
        } catch (Exception e) {
            authException = new LockedException("Wrong login.");
        }

        if (account != null) {

            if (account.isAccountNonLocked()) {

                if (account.getRoles().contains("ROLE_ADMINISTRATOR")) {

                    authException = new LockedException("Wrong password");

                } else if (account.getFailedAttempt() < AccountService.MAX_FAILED_ATTEMPTS - 1) {

                    accountService.increaseFailedAttempt(account);
                    authException = new LockedException(
                            String.format("Your account will be locked after %d incorrect attempt.",
                                    AccountService.MAX_FAILED_ATTEMPTS - (account.getFailedAttempt() + 1))
                    );
                } else {

                    logService.log(
                            LocalDateTime.now(),
                            Action.BRUTE_FORCE,
                            account.getEmail(),
                            request.getRequestURI()
                    );

                    accountService.lockAccount(account, account.getEmail());
                    authException = new LockedException("Your account has been locked due to 5 failed attempts."
                            + " It will be unlocked after 24 hours.");
                }

            } else {

                if (accountService.unlockWhenTimeExpired(account)) {
                    authException = new LockedException("Your account has been unlocked. Please try to login again.");
                } else {
                    authException = new LockedException("Your account was locked. Please try again later.");
                }
            }

        }

        Map<String, Object> errorData = new LinkedHashMap<>();
        HttpStatus status = HttpStatus.UNAUTHORIZED;

        errorData.put("timestamp", LocalDateTime.now().toString());
        errorData.put("status", status.value());
        errorData.put("error", status.getReasonPhrase());
        errorData.put("message", authException.getMessage());
        errorData.put("path", request.getRequestURI());

        PrintWriter writer = response.getWriter();

        response.setStatus(status.value());
        writer.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(errorData));
    }

    private String getLoginFromAuthorization(String decodedAuthorization) {

        if (decodedAuthorization == null || decodedAuthorization.length() < 11) {
            return "";
        }

        String encodedCredentials = decodedAuthorization.split(" ")[1];
        byte[] byteArrayDecodedAuthorization = Base64.getDecoder().decode(encodedCredentials);

        return new String(byteArrayDecodedAuthorization)
                .split(":")[0];
    }
}
