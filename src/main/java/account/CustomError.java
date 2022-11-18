package account;


import java.util.List;

public class CustomError {
    private final String timestamp;
    private final int status;
    private final String error;
    private final List<String> message;
    private final String path;

    public CustomError(String timestamp, int status, String error, List<String> message, String path) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public List<String> getMessage() {
        return message;
    }

    public String getPath() {
        return path;
    }
}
