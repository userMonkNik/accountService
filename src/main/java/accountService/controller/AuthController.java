package accountService.controller;

import accountService.entity.Account;
import accountService.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api")
public class AuthController {
    private final AccountService accountService;
    @Autowired
    public AuthController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/auth/signup")
    public ResponseEntity<Account> signUp(@Valid @RequestBody Account account) {

        return new ResponseEntity<>(accountService.signup(account), HttpStatus.OK);
    }

    @GetMapping("/empl/payment")
    public ResponseEntity<Account> testGet(Authentication auth) {

        return new ResponseEntity<>(accountService.getCurrentUser(auth.getName()),HttpStatus.OK);
    }

}
