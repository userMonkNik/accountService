package account.dto;

import javax.validation.constraints.Size;

public class NewPassword {
    @Size(min = 12, message = "The password length must be at least 12 chars!")
    private String password;

    public String getPassword() {
        return password;
    }
}
