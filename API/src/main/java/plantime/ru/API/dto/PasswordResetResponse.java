package plantime.ru.API.dto;

public class PasswordResetResponse {
    private String message;

    public PasswordResetResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}