package account.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Role not found!")
public class RoleNotExistsException extends RuntimeException {
    public RoleNotExistsException() {
        super();
    }
}
