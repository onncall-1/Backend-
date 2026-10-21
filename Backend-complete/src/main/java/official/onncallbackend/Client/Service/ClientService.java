package official.onncallbackend.Client.Service;

import official.onncallbackend.Auth.Security.AccessGuard;
import official.onncallbackend.Client.Client;
import official.onncallbackend.Client.Repository.ClientRepository;
import official.onncallbackend.common.ApiException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClientService {

    private final ClientRepository clientRepository;
    private final AccessGuard guard;

    public ClientService(ClientRepository clientRepository, AccessGuard guard) {
        this.clientRepository = clientRepository;
        this.guard = guard;
    }

    /** Admin only (enforced in SecurityConfig). Normal clients are created through /api/auth/register. */
    public Client createClient(Client client) {
        return clientRepository.save(client);
    }

    public List<Client> getAllClients() {
        return clientRepository.findAll();
    }

    public Client getClientById(Long id) {
        guard.requireClient(id);
        return find(id);
    }

    public Client updateClient(Long id, Client client) {
        guard.requireClient(id);
        Client existing = find(id);

        if (client.getName() != null && !client.getName().isBlank()) {
            existing.setName(client.getName());
        }
        existing.setProfilePhoto(client.getProfilePhoto());
        existing.setAddress(client.getAddress());
        existing.setLatitude(client.getLatitude());
        existing.setLongitude(client.getLongitude());

        return clientRepository.save(existing);
    }

    public void deleteClient(Long id) {
        guard.requireAdmin();
        clientRepository.delete(find(id));
    }

    private Client find(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Client not found with id: " + id));
    }
}
