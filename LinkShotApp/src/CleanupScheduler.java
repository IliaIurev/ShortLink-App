import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CleanupScheduler {
    private final LinkShortService service;
    private final ScheduledExecutorService scheduler;

    public CleanupScheduler(LinkShortService service) {
        this.service = service;
        this.scheduler = Executors.newScheduledThreadPool(1);
    }

    public void start() {
        // Очистка каждые 30 минут
        scheduler.scheduleAtFixedRate(() -> {
            service.cleanupExpiredLinks();
            System.out.println("[" + java.time.LocalDateTime.now() + "] Выполнена очистка просроченных ссылок");
        }, 0, 30, TimeUnit.MINUTES);
    }

    public void stop() {
        scheduler.shutdown();
    }
}
