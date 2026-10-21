package official.onncallbackend.Config;

import official.onncallbackend.Auth.Service.PhoneNumbers;
import official.onncallbackend.Service.Enum.ServiceType;
import official.onncallbackend.Service.Repository.ServiceRepository;
import official.onncallbackend.Service.Service;
import official.onncallbackend.User.Enum.UserRole;
import official.onncallbackend.User.Repository.UserRepository;
import official.onncallbackend.User.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * On startup: creates the admin account from ADMIN_PHONE (if set and missing),
 * and seeds one service per ServiceType so the frontend has a catalogue to show.
 * Prices are placeholders. Change them through PUT /api/services/{id}.
 */
@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class DataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final ServiceRepository serviceRepository;
    private final String adminPhone;

    public DataSeeder(UserRepository userRepository,
                      ServiceRepository serviceRepository,
                      @Value("${app.admin.phone:}") String adminPhone) {
        this.userRepository = userRepository;
        this.serviceRepository = serviceRepository;
        this.adminPhone = adminPhone;
    }

    @Override
    public void run(ApplicationArguments args) {
        seedAdmin();
        seedServices();
    }

    private void seedAdmin() {
        if (adminPhone == null || adminPhone.isBlank()) {
            return;
        }
        String phone = PhoneNumbers.normalize(adminPhone);
        if (!userRepository.existsByPhone(phone)) {
            User admin = new User();
            admin.setPhone(phone);
            admin.setRole(UserRole.ADMIN);
            userRepository.save(admin);
            log.info("Seeded admin account for {}", phone);
        }
    }

    private void seedServices() {
        for (ServiceType type : ServiceType.values()) {
            if (serviceRepository.existsByType(type)) {
                continue;
            }
            Service service = new Service();
            service.setType(type);
            service.setName(displayName(type));
            service.setDescription("Default category. Edit the name, description and price.");
            service.setBasePrice(defaultPrice(type));
            serviceRepository.save(service);
        }
    }

    private String displayName(ServiceType type) {
        return switch (type) {
            case PLUMBER -> "Plumber";
            case ELECTRICIAN -> "Electrician";
            case CARPENTER -> "Carpenter";
            case DAILY_WAGER -> "Daily wage worker";
        };
    }

    private double defaultPrice(ServiceType type) {
        return switch (type) {
            case PLUMBER -> 500.0;
            case ELECTRICIAN -> 500.0;
            case CARPENTER -> 600.0;
            case DAILY_WAGER -> 800.0;
        };
    }
}
