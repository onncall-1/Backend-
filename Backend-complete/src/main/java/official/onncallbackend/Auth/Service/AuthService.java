package official.onncallbackend.Auth.Service;

import official.onncallbackend.Auth.Dto.AuthDtos.AuthResponse;
import official.onncallbackend.Auth.Dto.AuthDtos.LoginRequest;
import official.onncallbackend.Auth.Dto.AuthDtos.MeResponse;
import official.onncallbackend.Auth.Dto.AuthDtos.RegisterRequest;
import official.onncallbackend.Auth.Dto.AuthDtos.UserSummary;
import official.onncallbackend.Auth.Security.AuthPrincipal;
import official.onncallbackend.Client.Client;
import official.onncallbackend.Client.Repository.ClientRepository;
import official.onncallbackend.Distributor.Distributor;
import official.onncallbackend.Distributor.Repository.DistributorRepository;
import official.onncallbackend.Provider.Provider;
import official.onncallbackend.Provider.Repository.ProviderRepository;
import official.onncallbackend.User.Enum.UserRole;
import official.onncallbackend.User.Repository.UserRepository;
import official.onncallbackend.User.User;
import official.onncallbackend.common.ApiException;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final ProviderRepository providerRepository;
    private final DistributorRepository distributorRepository;
    private final OtpService otpService;
    private final JwtService jwtService;
    private final PhoneTokenVerifier phoneTokenVerifier;

    public AuthService(UserRepository userRepository,
                       ClientRepository clientRepository,
                       ProviderRepository providerRepository,
                       DistributorRepository distributorRepository,
                       OtpService otpService,
                       JwtService jwtService,
                       PhoneTokenVerifier phoneTokenVerifier) {
        this.userRepository = userRepository;
        this.clientRepository = clientRepository;
        this.providerRepository = providerRepository;
        this.distributorRepository = distributorRepository;
        this.otpService = otpService;
        this.jwtService = jwtService;
        this.phoneTokenVerifier = phoneTokenVerifier;
    }

    // ---------- Firebase sign-in (production) ----------

    public AuthResponse loginWithFirebase(String idToken) {
        String phone = phoneTokenVerifier.verifyPhone(idToken);
        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> ApiException.notFound("No account for this phone number. Register first."));
        return tokenFor(user, profileIdFor(user));
    }

    public AuthResponse registerWithFirebase(String idToken, UserRole role, RegisterRequest profile) {
        String phone = phoneTokenVerifier.verifyPhone(idToken);
        RegisterRequest req = new RegisterRequest(phone, null, role,
                profile.name(), profile.businessName(), profile.contactName(), profile.bio(),
                profile.address(), profile.latitude(), profile.longitude());
        ensureNewAccount(phone, req);
        return createAccount(phone, req);
    }

    // ---------- Code login (development) ----------

    /** Creates the login account and its profile. Input is checked before the code is used. */
    public AuthResponse register(RegisterRequest req) {
        String phone = PhoneNumbers.normalize(req.phone());
        ensureNewAccount(phone, req);
        otpService.verify(phone, req.otp());
        return createAccount(phone, req);
    }

    /** Logs in an existing account. Checks the account before the code is used. */
    public AuthResponse login(LoginRequest req) {
        String phone = PhoneNumbers.normalize(req.phone());

        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> ApiException.notFound(
                        "No account for this phone number. Register first."));

        otpService.verify(phone, req.otp());

        return tokenFor(user, profileIdFor(user));
    }

    public MeResponse me(AuthPrincipal principal) {
        User user = userRepository.findById(principal.userId())
                .orElseThrow(() -> ApiException.unauthorized("This account no longer exists"));
        return new MeResponse(user.getId(), user.getPhone(), user.getRole(), profileIdFor(user));
    }

    // ---------- shared ----------

    private void ensureNewAccount(String phone, RegisterRequest req) {
        if (req.role() == null) {
            throw ApiException.badRequest("Role is required: CLIENT, PROVIDER or DISTRIBUTOR");
        }
        if (req.role() == UserRole.ADMIN) {
            throw ApiException.forbidden("Admin accounts cannot be self-registered");
        }
        if (userRepository.existsByPhone(phone)) {
            throw ApiException.conflict("An account already exists for this phone number. Log in instead.");
        }
        validateProfile(req);
    }

    private AuthResponse createAccount(String phone, RegisterRequest req) {
        User user = new User();
        user.setPhone(phone);
        user.setRole(req.role());
        String userName = req.name() != null && !req.name().isBlank() ? req.name().trim() : "User " + phone;
        user.setName(userName);
        user.setEmail(phone.replace("+", "") + "@onncall.local");
        userRepository.save(user);

        Long profileId = createProfile(user, req);
        return tokenFor(user, profileId);
    }

    private AuthResponse tokenFor(User user, Long profileId) {
        return new AuthResponse(
                jwtService.createToken(user),
                "Bearer",
                jwtService.expiresInSeconds(),
                new UserSummary(user.getId(), user.getPhone(), user.getRole(), profileId));
    }

    private Long profileIdFor(User user) {
        return switch (user.getRole()) {
            case CLIENT -> clientRepository.findByUserId(user.getId()).map(Client::getId).orElse(null);
            case PROVIDER -> providerRepository.findByUserId(user.getId()).map(Provider::getId).orElse(null);
            case DISTRIBUTOR -> distributorRepository.findByUserId(user.getId()).map(Distributor::getId).orElse(null);
            case ADMIN -> null;
        };
    }

    private void validateProfile(RegisterRequest req) {
        switch (req.role()) {
            case CLIENT, PROVIDER -> {
                if (req.name() == null || req.name().isBlank()) {
                    throw ApiException.badRequest("name is required");
                }
            }
            case DISTRIBUTOR -> {
                if (req.businessName() == null || req.businessName().isBlank()) {
                    throw ApiException.badRequest("businessName is required");
                }
            }
            default -> throw ApiException.forbidden("Role cannot self-register");
        }
    }

    private Long createProfile(User user, RegisterRequest req) {
        switch (req.role()) {
            case CLIENT -> {
                Client client = new Client();
                client.setUser(user);
                client.setName(req.name());
                client.setAddress(req.address());
                client.setLatitude(req.latitude());
                client.setLongitude(req.longitude());
                return clientRepository.save(client).getId();
            }
            case PROVIDER -> {
                Provider provider = new Provider();
                provider.setUser(user);
                provider.setName(req.name());
                provider.setBio(req.bio());
                provider.setLatitude(req.latitude());
                provider.setLongitude(req.longitude());
                return providerRepository.save(provider).getId();
            }
            case DISTRIBUTOR -> {
                Distributor distributor = new Distributor();
                distributor.setUser(user);
                distributor.setBusinessName(req.businessName());
                distributor.setContactName(req.contactName());
                distributor.setAddress(req.address());
                distributor.setLatitude(req.latitude());
                distributor.setLongitude(req.longitude());
                return distributorRepository.save(distributor).getId();
            }
            default -> throw ApiException.forbidden("Role cannot self-register");
        }
    }
}
