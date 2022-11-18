package account.controller;

import account.CustomError;
import account.dto.AddPaymentResponse;
import account.entity.PaymentDetails;
import account.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/acct")
@Validated
public class AccountantController {

    private final AccountService accountService;

    @Autowired
    AccountantController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/payments")
    public ResponseEntity<AddPaymentResponse> addPaymentDetails(@Valid @RequestBody List<PaymentDetails> paymentDetailsList) {



        return new ResponseEntity<>(accountService.transactionalAddPaymentDetails(paymentDetailsList), HttpStatus.OK);

    }


    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public CustomError handleValidationException(ConstraintViolationException ex, WebRequest request) {

        List<String> errorList = ex.getConstraintViolations()
                .stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toList());

        return new CustomError(
                LocalDateTime.now().toString(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.name(),
                errorList,
                request.getDescription(false));
    }
}