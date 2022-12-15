package account.service;

import account.entity.SecurityLogs;
import account.repository.SecurityLogsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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

    public List<SecurityLogs> getSecurityLogsListSortedById() {

        return repository.findAll().stream()
                .sorted(Comparator.comparing(SecurityLogs::getId))
                .collect(Collectors.toList());
    }
}
