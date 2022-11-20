package account.controller;

import account.dto.AccountPaymentDetails;
import account.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/empl")
public class EmployeeController {
    private final AccountService accountService;
    @Autowired
    public EmployeeController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/payment")
    public ResponseEntity<AccountPaymentDetails> getPaymentByPeriod(Authentication auth, @RequestParam String period) {

        return new ResponseEntity<>(accountService.getEmployeePaymentDetailsByPeriod(auth.getName(), period), HttpStatus.OK);
    }
}
