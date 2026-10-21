package official.onncallbackend.Auth.Security;

import official.onncallbackend.Client.Client;
import official.onncallbackend.Client.Repository.ClientRepository;
import official.onncallbackend.Distributor.Distributor;
import official.onncallbackend.Distributor.Repository.DistributorRepository;
import official.onncallbackend.Provider.Provider;
import official.onncallbackend.Provider.Repository.ProviderRepository;
import official.onncallbackend.User.Enum.UserRole;
import official.onncallbackend.common.ApiException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Ownership checks. Role rules in SecurityConfig decide who may call an endpoint at all;
 * these methods decide whether the caller may act on a specific record.
 */
@Component
public class AccessGuard {

    private final ClientRepository clientRepository;
    private final ProviderRepository providerRepository;
    private final DistributorRepository distributorRepository;

    public AccessGuard(ClientRepository clientRepository,
                       ProviderRepository providerRepository,
                       DistributorRepository distributorRepository) {
        this.clientRepository = clientRepository;
        this.providerRepository = providerRepository;
        this.distributorRepository = distributorRepository;
    }

    public Optional<AuthPrincipal> currentPrincipal() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AuthPrincipal p) {
            return Optional.of(p);
        }
        return Optional.empty();
    }

    public AuthPrincipal require() {
        return currentPrincipal()
                .orElseThrow(() -> ApiException.unauthorized("Authentication required"));
    }

    public boolean isAdmin() {
        return currentPrincipal().map(p -> p.role() == UserRole.ADMIN).orElse(false);
    }

    public void requireAdmin() {
        if (!isAdmin()) {
            throw ApiException.forbidden("Only an admin can do this");
        }
    }

    public void requireSelfOrAdmin(Long userId) {
        AuthPrincipal p = require();
        if (!isAdmin() && !p.userId().equals(userId)) {
            throw ApiException.forbidden("You can only access your own account");
        }
    }

    public Long currentClientId() {
        AuthPrincipal p = require();
        if (p.role() != UserRole.CLIENT) {
            return null;
        }
        return clientRepository.findByUserId(p.userId()).map(Client::getId).orElse(null);
    }

    public Long currentProviderId() {
        AuthPrincipal p = require();
        if (p.role() != UserRole.PROVIDER) {
            return null;
        }
        return providerRepository.findByUserId(p.userId()).map(Provider::getId).orElse(null);
    }

    public Long currentDistributorId() {
        AuthPrincipal p = require();
        if (p.role() != UserRole.DISTRIBUTOR) {
            return null;
        }
        return distributorRepository.findByUserId(p.userId()).map(Distributor::getId).orElse(null);
    }

    public void requireClient(Long clientId) {
        if (isAdmin()) return;
        Long mine = currentClientId();
        if (mine == null || !mine.equals(clientId)) {
            throw ApiException.forbidden("You can only act on your own client account");
        }
    }

    public void requireProvider(Long providerId) {
        if (isAdmin()) return;
        Long mine = currentProviderId();
        if (mine == null || !mine.equals(providerId)) {
            throw ApiException.forbidden("You can only act on your own provider account");
        }
    }

    public void requireDistributor(Long distributorId) {
        if (isAdmin()) return;
        Long mine = currentDistributorId();
        if (mine == null || !mine.equals(distributorId)) {
            throw ApiException.forbidden("You can only act on your own distributor account");
        }
    }

    /** True if the caller is the client or the provider on a request/job (or an admin). */
    public boolean isParticipant(Long clientId, Long providerId) {
        if (isAdmin()) return true;
        return (clientId != null && clientId.equals(currentClientId()))
                || (providerId != null && providerId.equals(currentProviderId()));
    }

    public void requireParticipant(Long clientId, Long providerId) {
        if (!isParticipant(clientId, providerId)) {
            throw ApiException.forbidden("You are not a party to this record");
        }
    }
}
