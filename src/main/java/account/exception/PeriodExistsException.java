package account.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class PeriodExistsException extends RuntimeException{
    public PeriodExistsException(String msg) {
        super("Period already exists for:" + msg);
    }
}
