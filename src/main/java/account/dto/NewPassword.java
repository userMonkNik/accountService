package account.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class NewPassword {
    @NotNull(message = "Must not be null")
    @Size(min = 12, message = "The password length must be at least 12 chars!")
    @JsonProperty("new_password")
    private String password;

    public String getPassword() {
        return password;
    }
}
