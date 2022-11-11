package account.controller;

import account.CustomError;
import account.dto.ChangePasswordResponse;
import account.dto.NewPassword;
import account.entity.Account;
import account.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import javax.validation.Valid;
import java.time.LocalDateTime;

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

    @PostMapping("/auth/changepass")
    public ResponseEntity<ChangePasswordResponse> changePassword(Authentication auth, @Validated @RequestBody NewPassword password) {

        return new ResponseEntity<>(
                accountService.changePassword(auth.getName(), password),
                HttpStatus.OK);
    }

    @GetMapping("/empl/payment")
    public ResponseEntity<Account> testGet(Authentication auth) {

        return new ResponseEntity<>(accountService.getCurrentUser(auth.getName()),HttpStatus.OK);
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public CustomError handleValidationException(MethodArgumentNotValidException ex, WebRequest request) {

        String errorMsg = ex.getBindingResult().getFieldError().getDefaultMessage();


        return new CustomError(
                LocalDateTime.now().toString(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.name(),
                errorMsg,
                request.getDescription(false));
    }

}
