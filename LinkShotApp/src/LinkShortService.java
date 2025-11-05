import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class LinkShortService {
    private final Map<String, ShortLink> shortCodeToLink = new ConcurrentHashMap<>();
    private final Map<UUID, Set<String>> userLinks = new ConcurrentHashMap<>();
    private final int DEFAULT_VALIDITY_HOURS = 24;
    private final int DEFAULT_CLICK_LIMIT = 100;

    public String createShortLink(String originalUrl, UUID userId) {
        return createShortLink(originalUrl, userId, DEFAULT_CLICK_LIMIT, DEFAULT_VALIDITY_HOURS);
    }

    public String createShortLink(String originalUrl, UUID userId, int clickLimit, int validityHours) {
        ShortLink link = new ShortLink(originalUrl, userId, clickLimit, validityHours);

        shortCodeToLink.put(link.getShortCode(), link);
        userLinks.computeIfAbsent(userId, k -> new HashSet<>()).add(link.getShortCode());

        return link.getShortCode();
    }

    public Optional<String> redirect(String shortCode) {
        ShortLink link = shortCodeToLink.get(shortCode);

        if (link == null || !link.isValid()) {
            return Optional.empty();
        }

        link.incrementClickCount();
        return Optional.of(link.getOriginalUrl());
    }

    public void cleanupExpiredLinks() {
        LocalDateTime now = LocalDateTime.now();
        shortCodeToLink.entrySet().removeIf(entry ->
                entry.getValue().getExpirationTime().isBefore(now)
        );
    }

    public List<ShortLink> getUserLinks(UUID userId) {
        Set<String> userShortCodes = userLinks.get(userId);
        if (userShortCodes == null) return Collections.emptyList();

        return userShortCodes.stream()
                .map(shortCodeToLink::get)
                .filter(Objects::nonNull)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    public Optional<Notification> checkLinkStatus(String shortCode, UUID userId) {
        ShortLink link = shortCodeToLink.get(shortCode);
        if (link == null || !link.getUserId().equals(userId)) {
            return Optional.empty();
        }

        if (!link.isActive() && link.getClickCount() >= link.getClickLimit()) {
            return Optional.of(new Notification(
                    "Лимит переходов исчерпан",
                    "Ссылка " + shortCode + " достигла лимита в " + link.getClickLimit() + " переходов"
            ));
        }

        if (LocalDateTime.now().isAfter(link.getExpirationTime())) {
            return Optional.of(new Notification(
                    "Срок действия ссылки истек",
                    "Ссылка " + shortCode + " создана " + link.getCreationTime()
            ));
        }

        return Optional.empty();
    }
}
