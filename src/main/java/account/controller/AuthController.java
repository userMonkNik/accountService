package account.controller;

import account.dto.ChangePasswordResponse;
import account.dto.NewPassword;
import account.entity.Account;
import account.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.neo4j.Neo4jProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AccountService accountService;
    @Autowired
    public AuthController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/signup")
    public ResponseEntity<Account> signUp(@Valid @RequestBody Account account) {

        return new ResponseEntity<>(accountService.signup(account), HttpStatus.OK);
    }

    @PostMapping("/changepass")
    public ResponseEntity<ChangePasswordResponse> changePassword(Authentication auth, @Valid @RequestBody NewPassword password) {

        System.out.println(auth.getName());
        return new ResponseEntity<>(
                accountService.changePassword(auth.getName(), password),
                HttpStatus.OK);
    }

}
