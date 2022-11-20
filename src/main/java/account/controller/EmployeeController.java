package account.controller;

import account.dto.AccountPaymentDetails;
import account.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Pattern;
import java.util.List;

@RestController
@RequestMapping("/api/empl")
@Validated
public class EmployeeController {
    private final AccountService accountService;
    @Autowired
    public EmployeeController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/payment")
    public ResponseEntity<List<AccountPaymentDetails>> getPaymentByPeriod(Authentication auth,
                                                                    @Pattern(regexp = "(0\\d|1[0-2])-2\\d{3}", message = "Wrong Date!")
                                                                    @RequestParam(required = false) String period) {
        if (period == null) {

            return new ResponseEntity<>(accountService.getAllEmployeePayments(auth.getName()), HttpStatus.OK);
        } else {

            return new ResponseEntity<>(List.of(accountService.getEmployeePaymentDetailsByPeriod(auth.getName(), period)),
                    HttpStatus.OK);
        }
    }
}
