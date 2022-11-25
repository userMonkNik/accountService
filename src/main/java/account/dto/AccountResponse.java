package account.dto;

public class AccountResponse {

    private String user;
    private String status;

    public AccountResponse(String user, String status) {
        this.user = user;
        this.status = status;
    }

    public String getUser() {
        return user;
    }

    public String getStatus() {
        return status;
    }
}
