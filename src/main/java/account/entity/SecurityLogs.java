package account.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Entity
public class SecurityLogs {
    @Id
    @GeneratedValue
    private long id;
    private LocalDateTime date;
    private String action;
    private String subject;
    private String object;
    private String path;

    public SecurityLogs() {

    }

    public SecurityLogs(LocalDateTime date, String action, String subject, String object, String path) {
        this.date = date;
        this.action = action;
        this.subject = subject;
        this.object = object;
        this.path = path;
    }

    public long getId() {
        return id;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public String getAction() {
        return action;
    }

    public String getSubject() {
        return subject;
    }

    public String getObject() {
        return object;
    }

    public String getPath() {
        return path;
    }
}
