package official.onncallbackend.Service;

import official.onncallbackend.Service.Enum.ServiceStatus;
import official.onncallbackend.Service.Enum.ServiceType;
import official.onncallbackend.Service.Repository.ServiceRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServiceService {

    private final ServiceRepository serviceRepository;

    public ServiceService(ServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    public Service createService(Service service) {

        if (serviceRepository.existsByType(service.getType())) {
            throw new RuntimeException(
                    "Service already exists: " + service.getType()
            );
        }

        return serviceRepository.save(service);
    }

    public List<Service> getAllServices() {

        return serviceRepository.findAll();
    }

    public Service getServiceById(Long id) {

        return serviceRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Service not found with id: " + id
                        ));
    }

    public Service getServiceByType(ServiceType type) {

        return serviceRepository.findByType(type)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Service not found: " + type
                        ));
    }

    public List<Service> getActiveServices() {

        return serviceRepository.findByStatus(
                ServiceStatus.ACTIVE
        );
    }

    public Service updateService(Long id, Service service) {

        Service existingService =
                serviceRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Service not found with id: " + id
                                ));

        existingService.setType(service.getType());
        existingService.setName(service.getName());
        existingService.setDescription(service.getDescription());
        existingService.setBasePrice(service.getBasePrice());
        existingService.setStatus(service.getStatus());

        return serviceRepository.save(existingService);
    }

    public void deleteService(Long id) {

        Service existingService =
                serviceRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Service not found with id: " + id
                                ));

        serviceRepository.delete(existingService);
    }
}