package account.controller;

import account.entity.Account;
import account.service.AccountService;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdministratorController {

    private final AccountService accountService;

    @Autowired
    public AdministratorController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/user")
    public ResponseEntity<List<Account>> getAllUsers() {

        return new ResponseEntity<>(accountService.getAllUsersSortedById(), HttpStatus.OK);
    }

    @DeleteMapping("/user/{email}")
    public ResponseEntity<?> deleteUser(@PathVariable String email) {

        return new ResponseEntity<>(accountService.deleteAccount(email), HttpStatus.OK);
    }
}
