
package official.onncallbackend.Notification;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(
            NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping
    public Notification createNotification(
            @RequestBody Notification notification) {
        return notificationService.createNotification(notification);
    }

    @GetMapping("/user/{userId}")
    public List<Notification> getNotifications(
            @PathVariable Long userId) {
        return notificationService.getUserNotifications(userId);
    }

    @GetMapping("/user/{userId}/unread")
    public List<Notification> getUnreadNotifications(
            @PathVariable Long userId) {
        return notificationService.getUnreadNotifications(userId);
    }

    @PatchMapping("/{notificationId}/read")
    public Notification markAsRead(
            @PathVariable Long notificationId,
            @RequestParam Long userId) {
        return notificationService.markAsRead(notificationId, userId);
    }

    @DeleteMapping("/{notificationId}")
    public void deleteNotification(
            @PathVariable Long notificationId,
            @RequestParam Long userId) {
        notificationService.deleteNotification(notificationId, userId);
    }
}
