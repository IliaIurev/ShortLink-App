import java.time.LocalDateTime;

public class Notification {
    private final String title;
    private final String message;
    private final LocalDateTime timestamp;

    public Notification(String title, String message) {
        this.title = title;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    // Getters
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public LocalDateTime getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return String.format("[%s] %s: %s", timestamp, title, message);
    }
}
