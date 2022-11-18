package account.controller;

import account.CustomError;
import account.dto.ChangePasswordResponse;
import account.dto.NewPassword;
import account.entity.Account;
import account.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

        return new ResponseEntity<>(
                accountService.changePassword(auth.getName(), password),
                HttpStatus.OK);
    }



    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public CustomError handleValidationException(MethodArgumentNotValidException ex, WebRequest request) {

        List<FieldError> fieldErrorList = ex.getFieldErrors();
        List<String> errorList = fieldErrorList.stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.toList());


        return new CustomError(
                LocalDateTime.now().toString(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.name(),
                errorList,
                request.getDescription(false));
    }

}
