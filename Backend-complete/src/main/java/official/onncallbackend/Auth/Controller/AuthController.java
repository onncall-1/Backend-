package official.onncallbackend.Auth.Controller;

import jakarta.validation.Valid;
import official.onncallbackend.Auth.Dto.AuthDtos.AuthResponse;
import official.onncallbackend.Auth.Dto.AuthDtos.FirebaseLoginRequest;
import official.onncallbackend.Auth.Dto.AuthDtos.FirebaseRegisterRequest;
import official.onncallbackend.Auth.Dto.AuthDtos.LoginRequest;
import official.onncallbackend.Auth.Dto.AuthDtos.MeResponse;
import official.onncallbackend.Auth.Dto.AuthDtos.OtpRequest;
import official.onncallbackend.Auth.Dto.AuthDtos.OtpRequestResponse;
import official.onncallbackend.Auth.Dto.AuthDtos.RegisterRequest;
import official.onncallbackend.Auth.Security.AccessGuard;
import official.onncallbackend.Auth.Service.AuthService;
import official.onncallbackend.Auth.Service.OtpService;
import official.onncallbackend.common.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final OtpService otpService;
    private final AuthService authService;
    private final AccessGuard accessGuard;
    private final boolean otpEnabled;

    public AuthController(OtpService otpService,
                          AuthService authService,
                          AccessGuard accessGuard,
                          @Value("${app.auth.otp-enabled}") boolean otpEnabled) {
        this.otpService = otpService;
        this.authService = authService;
        this.accessGuard = accessGuard;
        this.otpEnabled = otpEnabled;
    }

    private void requireOtpEnabled() {
        if (!otpEnabled) {
            throw ApiException.forbidden("Code login is turned off. Sign in with Firebase instead.");
        }
    }

    // ---- Firebase phone sign-in (production) ----

    /** The frontend has signed in with Firebase. Pass its ID token. 404 if no account exists yet. */
    @PostMapping("/firebase/login")
    public AuthResponse firebaseLogin(@Valid @RequestBody FirebaseLoginRequest body) {
        return authService.loginWithFirebase(body.idToken());
    }

    /** Create an account after Firebase sign-in. Returns 201 with a token. */
    @PostMapping("/firebase/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse firebaseRegister(@Valid @RequestBody FirebaseRegisterRequest body) {
        RegisterRequest profile = new RegisterRequest(null, null, body.role(),
                body.name(), body.businessName(), body.contactName(), body.bio(),
                body.address(), body.latitude(), body.longitude());
        return authService.registerWithFirebase(body.idToken(), body.role(), profile);
    }

    // ---- Code login (development only, when app.auth.otp-enabled=true) ----

    @PostMapping("/otp/request")
    public OtpRequestResponse requestOtp(@Valid @RequestBody OtpRequest body) {
        requireOtpEnabled();
        return otpService.issue(body.phone());
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest body) {
        requireOtpEnabled();
        return authService.login(body);
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest body) {
        requireOtpEnabled();
        return authService.register(body);
    }

    /** The logged-in user and their profile id (clientId / providerId / distributorId). */
    @GetMapping("/me")
    public MeResponse me() {
        return authService.me(accessGuard.require());
    }
}
