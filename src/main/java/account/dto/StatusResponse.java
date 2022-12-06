package account.dto;

public class StatusResponse {
    public StatusResponse(String status) {
        this.status = status;
    }
    private String status;

    public String getStatus() {
        return status;
    }
}
