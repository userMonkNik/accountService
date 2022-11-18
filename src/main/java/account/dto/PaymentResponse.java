package account.dto;

public class PaymentResponse {
    public PaymentResponse(String status) {
        this.status = status;
    }
    private String status;

    public String getStatus() {
        return status;
    }
}
