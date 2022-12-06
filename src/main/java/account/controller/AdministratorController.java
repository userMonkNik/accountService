package account.controller;

import account.dto.AccountResponse;
import account.dto.ChangeAccess;
import account.dto.ChangeRole;
import account.dto.StatusResponse;
import account.entity.Account;
import account.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
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
    public ResponseEntity<AccountResponse> deleteUser(@PathVariable String email) {

        return new ResponseEntity<>(accountService.deleteAccount(email), HttpStatus.OK);
    }

    @PutMapping("/user/role")
    public ResponseEntity<Account> putUserRole(@Valid @RequestBody ChangeRole role) {

        return new ResponseEntity<>(accountService.changeUserRole(role), HttpStatus.OK);
    }

    @PutMapping("/user/access")
    public ResponseEntity<StatusResponse> putUserAccess(@Valid @RequestBody ChangeAccess changeAccess) {

        return new ResponseEntity<>(accountService.putUserAccess(changeAccess), HttpStatus.OK);
    }
}
