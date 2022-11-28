package account.dto;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

public class ChangeRole {

    @NotEmpty(message = "User cannot be empty or undefined")
    private String user;
    @NotEmpty(message = "User role cannot be empty or undefined")
    private String role;
    @Pattern(regexp = "(GRANT)|(REMOVE)",
            message = "Wrong option. Available options: GRANT or REMOVE")
    @NotNull(message = "Operation cannot be undefined")
    private String operation;

    public String getUser() {
        return user;
    }

    public String getRole() {
        return ("ROLE_" + role).toUpperCase();
    }

    public String getOperation() {
        return operation;
    }
}
