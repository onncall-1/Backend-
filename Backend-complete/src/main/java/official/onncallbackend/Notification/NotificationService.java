
package official.onncallbackend.Notification;

import official.onncallbackend.common.ApiException;

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
            throw ApiException.badRequest("User ID is required");
        }

        if (notification.getTitle() == null ||
                notification.getTitle().isBlank()) {
            throw ApiException.badRequest("Title is required");
        }

        if (notification.getMessage() == null ||
                notification.getMessage().isBlank()) {
            throw ApiException.badRequest("Message is required");
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
                        ApiException.notFound(
                                "Notification not found"));

        if (!notification.getUserId().equals(userId)) {
            throw ApiException.badRequest(
                    "Notification does not belong to this user");
        }

        notification.setRead(true);

        return notificationRepository.save(notification);
    }

    public void deleteNotification(Long notificationId, Long userId) {

        Notification notification = notificationRepository
                .findById(notificationId)
                .orElseThrow(() ->
                        ApiException.notFound(
                                "Notification not found"));

        if (!notification.getUserId().equals(userId)) {
            throw ApiException.badRequest(
                    "Notification does not belong to this user");
        }

        notificationRepository.delete(notification);
    }
}
