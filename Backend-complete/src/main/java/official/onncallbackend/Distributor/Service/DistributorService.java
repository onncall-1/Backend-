package official.onncallbackend.Distributor.Service;

import official.onncallbackend.Auth.Security.AccessGuard;
import official.onncallbackend.Distributor.Distributor;
import official.onncallbackend.Distributor.Enum.DistributorStatus;
import official.onncallbackend.Distributor.Repository.DistributorRepository;
import official.onncallbackend.common.ApiException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DistributorService {

    private final DistributorRepository distributorRepository;
    private final AccessGuard guard;

    public DistributorService(DistributorRepository distributorRepository, AccessGuard guard) {
        this.distributorRepository = distributorRepository;
        this.guard = guard;
    }

    /** Admin only (SecurityConfig). Distributors normally register through /api/auth/register. */
    public Distributor createDistributor(Distributor distributor) {
        if (distributor.getUser() == null || distributor.getUser().getId() == null) {
            throw ApiException.badRequest("A linked user account is required");
        }
        if (distributorRepository.existsByUserId(distributor.getUser().getId())) {
            throw ApiException.conflict("Distributor profile already exists for this user");
        }
        return distributorRepository.save(distributor);
    }

    public List<Distributor> getAllDistributors() {
        return distributorRepository.findAll();
    }

    public Distributor getDistributorById(Long id) {
        guard.requireDistributor(id);
        return find(id);
    }

    public Distributor getDistributorByUserId(Long userId) {
        guard.requireSelfOrAdmin(userId);
        return distributorRepository.findByUserId(userId)
                .orElseThrow(() -> ApiException.notFound("Distributor not found for user id: " + userId));
    }

    public List<Distributor> getActiveDistributors() {
        return distributorRepository.findByStatus(DistributorStatus.ACTIVE);
    }

    /** Distributors can edit their details. Only an admin can change status (e.g. SUSPENDED). */
    public Distributor updateDistributor(Long id, Distributor distributor) {
        guard.requireDistributor(id);
        Distributor existing = find(id);

        if (distributor.getBusinessName() != null && !distributor.getBusinessName().isBlank()) {
            existing.setBusinessName(distributor.getBusinessName());
        }
        existing.setContactName(distributor.getContactName());
        existing.setProfilePhoto(distributor.getProfilePhoto());
        existing.setAddress(distributor.getAddress());
        existing.setLatitude(distributor.getLatitude());
        existing.setLongitude(distributor.getLongitude());
        if (guard.isAdmin() && distributor.getStatus() != null) {
            existing.setStatus(distributor.getStatus());
        }

        return distributorRepository.save(existing);
    }

    public void deleteDistributor(Long id) {
        guard.requireAdmin();
        distributorRepository.delete(find(id));
    }

    private Distributor find(Long id) {
        return distributorRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Distributor not found with id: " + id));
    }
}
