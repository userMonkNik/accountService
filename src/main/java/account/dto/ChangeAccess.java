package account.dto;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

public class ChangeAccess {
    @NotEmpty(message = "Email cannot be empty.")
    private final String user;
    @Pattern(regexp = "(LOCK)|(UNLOCK)", message = "Wrong operation. Type: LOCK or UNLOCK.")
    @NotNull
    private final String operation;

    public ChangeAccess(String user, String operation) {
        this.user = user;
        this.operation = operation;
    }

    public String getUser() {
        return user;
    }

    public String getOperation() {
        return operation;
    }
}
