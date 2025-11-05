import java.time.LocalDateTime;
import java.util.UUID;

public class ShortLink {
    private final String id;
    private final String originalUrl;
    private final String shortCode;
    private final UUID userId;
    private final LocalDateTime creationTime;
    private final LocalDateTime expirationTime;
    private int clickLimit;
    private int clickCount;
    private boolean active;

    public ShortLink(String originalUrl, UUID userId, int clickLimit, int validityHours){
        this.id = UUID.randomUUID().toString();
        this.originalUrl = originalUrl;
        this.userId = userId;
        this.clickLimit = clickLimit;
        this.clickCount = 0;
        this.creationTime = LocalDateTime.now();
        this.expirationTime = creationTime.plusHours(validityHours);
        this.active = true;
        this.shortCode = generateShortCode();
    }

    private String generateShortCode(){
        return Base62.encode(originalUrl.hashCode() + userId.hashCode() + System.nanoTime());
    }

    public String getId() { return id; }
    public String getOriginalUrl() { return originalUrl; }
    public String getShortCode() { return shortCode; }
    public UUID getUserId() { return userId; }
    public LocalDateTime getCreationTime() { return creationTime; }
    public LocalDateTime getExpirationTime() { return expirationTime; }
    public int getClickLimit() { return clickLimit; }
    public int getClickCount() { return clickCount; }
    public boolean isActive() { return active; }

    public void incrementClickCount() {
        this.clickCount++;
        if (this.clickCount >= this.clickLimit) {
            this.active = false;
        }
    }

    public boolean isValid() {
        return active && LocalDateTime.now().isBefore(expirationTime);
    }
}
