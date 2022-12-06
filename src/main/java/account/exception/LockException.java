package account.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class LockException extends RuntimeException {
    public LockException(String msg) {
        super(msg);
    }
}
