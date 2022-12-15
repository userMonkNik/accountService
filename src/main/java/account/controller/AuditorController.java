package account.controller;

import account.entity.SecurityLogs;
import account.service.SecurityLogsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/security")
public class AuditorController {

    private final SecurityLogsService service;

    @Autowired
    public AuditorController(SecurityLogsService service) {
        this.service = service;
    }

    @GetMapping("/events")
    public ResponseEntity<List<SecurityLogs>> getSecurityLogs() {

        return new ResponseEntity<>(service.getSecurityLogsListSortedById(), HttpStatus.OK);
    }
}
