package account.controller;

import account.dto.PaymentResponse;
import account.entity.PaymentDetails;
import account.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

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
    public ResponseEntity<PaymentResponse> addPaymentDetails(@Valid @RequestBody List<PaymentDetails> paymentDetailsList) {

        return new ResponseEntity<>(accountService.transactionalAddPaymentDetails(paymentDetailsList), HttpStatus.OK);

    }

    @PutMapping("/payments")
    public ResponseEntity<PaymentResponse> addPaymentDetails(@Valid @RequestBody PaymentDetails paymentDetails) {

        return new ResponseEntity<>(accountService.updatePaymentDetails(paymentDetails), HttpStatus.OK);
    }


}
