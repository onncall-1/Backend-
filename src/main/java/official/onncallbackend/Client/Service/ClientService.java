package official.onncallbackend.Client.Service;

import official.onncallbackend.Client.Client;
import official.onncallbackend.Client.Repository.ClientRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClientService {

    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    // Create Client
    public Client createClient(Client client) {
        return clientRepository.save(client);
    }

    // Get all Clients
    public List<Client> getAllClients() {
        return clientRepository.findAll();
    }

    // Get Client by ID
    public Client getClientById(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client not found with id: " + id));
    }

    // Update Client
    public Client updateClient(Long id, Client client) {

        Client existingClient = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client not found with id: " + id));

        existingClient.setName(client.getName());
        existingClient.setProfilePhoto(client.getProfilePhoto());
        existingClient.setAddress(client.getAddress());
        existingClient.setLatitude(client.getLatitude());
        existingClient.setLongitude(client.getLongitude());

        return clientRepository.save(existingClient);
    }

    // Delete Client
    public void deleteClient(Long id) {

        Client existingClient = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client not found with id: " + id));

        clientRepository.delete(existingClient);
    }
}