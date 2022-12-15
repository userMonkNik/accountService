package account.service;

import account.entity.SecurityLogs;
import account.repository.SecurityLogsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Service
public class SecurityLogsService {
    private final SecurityLogsRepository repository;
    private final HttpServletRequest request;

    @Autowired
    public SecurityLogsService(SecurityLogsRepository repository, HttpServletRequest request) {
        this.repository = repository;
        this.request = request;
    }

    public void log(LocalDateTime date, String action, String subject, String object) {
        repository.save(
                new SecurityLogs(
                        date,
                        action,
                        subject,
                        object,
                        request.getRequestURI()
                )
        );
    }
}
