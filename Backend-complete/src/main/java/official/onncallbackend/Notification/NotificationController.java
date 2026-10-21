package official.onncallbackend.Notification;

import official.onncallbackend.Auth.Security.AccessGuard;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Users can only read and manage their own notifications.
 * Creating notifications is admin-only (SecurityConfig).
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final AccessGuard accessGuard;

    public NotificationController(NotificationService notificationService, AccessGuard accessGuard) {
        this.notificationService = notificationService;
        this.accessGuard = accessGuard;
    }

    @PostMapping
    public Notification createNotification(@RequestBody Notification notification) {
        return notificationService.createNotification(notification);
    }

    @GetMapping("/user/{userId}")
    public List<Notification> getNotifications(@PathVariable Long userId) {
        accessGuard.requireSelfOrAdmin(userId);
        return notificationService.getUserNotifications(userId);
    }

    @GetMapping("/user/{userId}/unread")
    public List<Notification> getUnreadNotifications(@PathVariable Long userId) {
        accessGuard.requireSelfOrAdmin(userId);
        return notificationService.getUnreadNotifications(userId);
    }

    @PatchMapping("/{notificationId}/read")
    public Notification markAsRead(@PathVariable Long notificationId, @RequestParam Long userId) {
        accessGuard.requireSelfOrAdmin(userId);
        return notificationService.markAsRead(notificationId, userId);
    }

    @DeleteMapping("/{notificationId}")
    public void deleteNotification(@PathVariable Long notificationId, @RequestParam Long userId) {
        accessGuard.requireSelfOrAdmin(userId);
        notificationService.deleteNotification(notificationId, userId);
    }
}
