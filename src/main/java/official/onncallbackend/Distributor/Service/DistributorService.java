package official.onncallbackend.Distributor.Service;

import official.onncallbackend.Distributor.Distributor;
import official.onncallbackend.Distributor.Enum.DistributorStatus;
import official.onncallbackend.Distributor.Repository.DistributorRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DistributorService {

    private final DistributorRepository distributorRepository;

    public DistributorService(
            DistributorRepository distributorRepository) {

        this.distributorRepository = distributorRepository;
    }

    public Distributor createDistributor(Distributor distributor) {

        if (distributor.getUser() == null) {
            throw new RuntimeException("User is required");
        }

        if (distributor.getUser().getId() == null) {
            throw new RuntimeException("User ID is required");
        }

        if (distributorRepository.existsByUserId(
                distributor.getUser().getId())) {

            throw new RuntimeException(
                    "Distributor profile already exists for this user"
            );
        }

        return distributorRepository.save(distributor);
    }

    public List<Distributor> getAllDistributors() {

        return distributorRepository.findAll();
    }

    public Distributor getDistributorById(Long id) {

        return distributorRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Distributor not found with id: " + id
                        ));
    }

    public Distributor getDistributorByUserId(Long userId) {

        return distributorRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Distributor not found for user id: "
                                        + userId
                        ));
    }

    public List<Distributor> getActiveDistributors() {

        return distributorRepository.findByStatus(
                DistributorStatus.ACTIVE
        );
    }

    public Distributor updateDistributor(
            Long id,
            Distributor distributor) {

        Distributor existingDistributor =
                distributorRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Distributor not found with id: "
                                                + id
                                ));

        existingDistributor.setBusinessName(
                distributor.getBusinessName()
        );

        existingDistributor.setContactName(
                distributor.getContactName()
        );

        existingDistributor.setProfilePhoto(
                distributor.getProfilePhoto()
        );

        existingDistributor.setAddress(
                distributor.getAddress()
        );

        existingDistributor.setLatitude(
                distributor.getLatitude()
        );

        existingDistributor.setLongitude(
                distributor.getLongitude()
        );

        existingDistributor.setStatus(
                distributor.getStatus()
        );

        return distributorRepository.save(existingDistributor);
    }

    public void deleteDistributor(Long id) {

        Distributor existingDistributor =
                distributorRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Distributor not found with id: "
                                                + id));

        distributorRepository.delete(existingDistributor);
    }
}