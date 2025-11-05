import java.awt.*;
import java.net.URI;
import java.util.*;
import java.util.List;

public class LinkShortApp {
    private final LinkShortService service = new LinkShortService();
    private final Scanner scanner = new Scanner(System.in);
    private UUID currentUserId;

    public static void main(String[] args) {
        new LinkShortApp().run();
    }

    public void run() {
        initializeUser();
        showMainMenu();
    }

    private void initializeUser() {
        System.out.println("=== Сервис сокращения ссылок ===");
        System.out.print("Введите ваш User ID (или нажмите Enter для генерации нового): ");
        String input = scanner.nextLine().trim();

        if (input.isEmpty()) {
            currentUserId = UUID.randomUUID();
            System.out.println("Ваш новый User ID: " + currentUserId);
        } else {
            try {
                currentUserId = UUID.fromString(input);
                System.out.println("Используется существующий User ID: " + currentUserId);
            } catch (IllegalArgumentException e) {
                System.out.println("Неверный формат UUID. Генерируем новый...");
                currentUserId = UUID.randomUUID();
                System.out.println("Ваш новый User ID: " + currentUserId);
            }
        }
    }

    private void showMainMenu() {
        while (true) {
            System.out.println("\n=== Главное меню ===");
            System.out.println("1. Создать короткую ссылку");
            System.out.println("2. Перейти по короткой ссылке");
            System.out.println("3. Мои ссылки");
            System.out.println("4. Проверить статус ссылки");
            System.out.println("5. Выход");
            System.out.print("Выберите действие: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1": createShortLink(); break;
                case "2": redirectToLink(); break;
                case "3": showUserLinks(); break;
                case "4": checkLinkStatus(); break;
                case "5":
                    System.out.println("До свидания!");
                    return;
                default:
                    System.out.println("Неверный выбор!");
            }
        }
    }

    private void createShortLink() {
        System.out.print("Введите длинную ссылку: ");
        String originalUrl = scanner.nextLine().trim();

        if (!isValidUrl(originalUrl)) {
            System.out.println("Ошибка: неверный формат URL");
            return;
        }

        System.out.print("Лимит переходов (по умолчанию 100): ");
        String limitInput = scanner.nextLine().trim();
        int clickLimit = limitInput.isEmpty() ? 100 : Integer.parseInt(limitInput);

        String shortCode = service.createShortLink(originalUrl, currentUserId, clickLimit, 24);
        String shortUrl = "clck.ru/" + shortCode;

        System.out.println("Короткая ссылка создана: " + shortUrl);
        System.out.println("Лимит переходов: " + clickLimit);
        System.out.println("Срок действия: 24 часа");
    }

    private void redirectToLink() {
        System.out.print("Введите короткий код (после clck.ru/): ");
        String shortCode = scanner.nextLine().trim();

        Optional<String> originalUrl = service.redirect(shortCode);

        if (originalUrl.isPresent()) {
            try {
                System.out.println("Перенаправление на: " + originalUrl.get());
                Desktop.getDesktop().browse(new URI(originalUrl.get()));
            } catch (Exception e) {
                System.out.println("Ошибка при открытии браузера: " + e.getMessage());
                System.out.println("Ссылка для ручного ввода: " + originalUrl.get());
            }
        } else {
            System.out.println("Ссылка не найдена или недоступна");

            // Проверяем причину недоступности
            Optional<Notification> notification = service.checkLinkStatus(shortCode, currentUserId);
            notification.ifPresent(System.out::println);
        }
    }

    private void showUserLinks() {
        List<ShortLink> links = service.getUserLinks(currentUserId);

        if (links.isEmpty()) {
            System.out.println("У вас нет созданных ссылок");
            return;
        }

        System.out.println("\n=== Ваши ссылки ===");
        for (ShortLink link : links) {
            String status = link.isValid() ? "АКТИВНА" : "НЕАКТИВНА";
            System.out.printf("clck.ru/%-8s → %-50s [%s]%n",
                    link.getShortCode(),
                    truncateUrl(link.getOriginalUrl()),
                    status);
            System.out.printf("    Переходы: %d/%d, Создана: %s, Истекает: %s%n",
                    link.getClickCount(), link.getClickLimit(),
                    link.getCreationTime(), link.getExpirationTime());
        }
    }

    private void checkLinkStatus() {
        System.out.print("Введите короткий код для проверки: ");
        String shortCode = scanner.nextLine().trim();

        Optional<Notification> notification = service.checkLinkStatus(shortCode, currentUserId);

        if (notification.isPresent()) {
            System.out.println(notification.get());
        } else {
            ShortLink link = service.getUserLinks(currentUserId).stream()
                    .filter(l -> l.getShortCode().equals(shortCode))
                    .findFirst()
                    .orElse(null);

            if (link != null) {
                System.out.println("Ссылка активна");
                System.out.printf("Переходы: %d/%d, Истекает: %s%n",
                        link.getClickCount(), link.getClickLimit(), link.getExpirationTime());
            } else {
                System.out.println("Ссылка не найдена или у вас нет к ней доступа");
            }
        }
    }

    private boolean isValidUrl(String url) {
        return url != null && (url.startsWith("http://") || url.startsWith("https://"));
    }

    private String truncateUrl(String url) {
        return url.length() > 50 ? url.substring(0, 47) + "..." : url;
    }
}
