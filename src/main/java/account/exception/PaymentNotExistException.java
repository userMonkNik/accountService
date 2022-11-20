package account.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Payment for this period doesn't exists")
public class PaymentNotExistException extends  RuntimeException{
    public PaymentNotExistException() {
        super();
    }
}
