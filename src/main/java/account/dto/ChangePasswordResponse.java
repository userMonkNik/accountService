package account.dto;

public class ChangePasswordResponse {
    private String email;
    private String status;

    public ChangePasswordResponse(String email, String status) {
        this.email = email;
        this.status = status;
    }

    public String getEmail() {
        return email;
    }

    public String getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "ChangePasswordResponse{" +
                "email='" + email + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
