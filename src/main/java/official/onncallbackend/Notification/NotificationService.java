
package official.onncallbackend.Notification;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(
            NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public Notification createNotification(Notification notification) {

        if (notification.getUserId() == null) {
            throw new IllegalArgumentException("User ID is required");
        }

        if (notification.getTitle() == null ||
                notification.getTitle().isBlank()) {
            throw new IllegalArgumentException("Title is required");
        }

        if (notification.getMessage() == null ||
                notification.getMessage().isBlank()) {
            throw new IllegalArgumentException("Message is required");
        }

        notification.setId(null);
        notification.setRead(false);

        return notificationRepository.save(notification);
    }

    public List<Notification> getUserNotifications(Long userId) {
        return notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<Notification> getUnreadNotifications(Long userId) {
        return notificationRepository
                .findByUserIdAndReadFalseOrderByCreatedAtDesc(userId);
    }

    public Notification markAsRead(Long notificationId, Long userId) {

        Notification notification = notificationRepository
                .findById(notificationId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Notification not found"));

        if (!notification.getUserId().equals(userId)) {
            throw new IllegalArgumentException(
                    "Notification does not belong to this user");
        }

        notification.setRead(true);

        return notificationRepository.save(notification);
    }

    public void deleteNotification(Long notificationId, Long userId) {

        Notification notification = notificationRepository
                .findById(notificationId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Notification not found"));

        if (!notification.getUserId().equals(userId)) {
            throw new IllegalArgumentException(
                    "Notification does not belong to this user");
        }

        notificationRepository.delete(notification);
    }
}
