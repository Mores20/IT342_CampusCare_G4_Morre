package edu.cit.morre.campuscare.features.auth;

import edu.cit.morre.campuscare.features.auth.dto.AuthResponse;
import edu.cit.morre.campuscare.shared.model.RefreshToken;
import edu.cit.morre.campuscare.shared.model.User;
import edu.cit.morre.campuscare.shared.repository.UserRepository;
import edu.cit.morre.campuscare.features.email.RefreshTokenService;
import edu.cit.morre.campuscare.shared.util.JwtUtil;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService; // ✅ added
    private final JwtUtil jwtUtil;                         // ✅ added
    private final UserRepository userRepository;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    public AuthController(AuthService authService,
                          RefreshTokenService refreshTokenService,
                          JwtUtil jwtUtil, UserRepository userRepository) {
        this.authService = authService;
        this.refreshTokenService = refreshTokenService;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String email = credentials.get("email");
        String password = credentials.get("password");

        AuthResponse response = authService.login(email, password);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(email);

        return ResponseEntity.ok(Map.of(
                "accessToken", response.getToken(),       // ✅ use getToken() not getAccessToken()
                "refreshToken", refreshToken.getToken(),
                "email", response.getEmail(),
                "firstName", response.getFirstName(),
                "lastName", response.getLastName(),
                "role", response.getRole()                // ✅ this is what Login.js reads
        ));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody Map<String, String> userData) {
        String firstName = userData.get("firstName");
        String lastName = userData.get("lastName");
        String email = userData.get("email");
        String password = userData.get("password");

        AuthResponse response = authService.register(firstName, lastName, email, password);
        return ResponseEntity.ok(response);
    }

    // ✅ New — refresh access token
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody Map<String, String> request) {
        String refreshTokenStr = request.get("refreshToken");
        try {
            RefreshToken refreshToken = refreshTokenService.validateRefreshToken(refreshTokenStr);
            String newAccessToken = jwtUtil.generateToken(refreshToken.getUser().getEmail());
            return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("message", e.getMessage()));
        }
    }

    // ✅ New — logout and delete refresh token
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody Map<String, String> request) {
        String refreshTokenStr = request.get("refreshToken");
        try {
            RefreshToken refreshToken = refreshTokenService.validateRefreshToken(refreshTokenStr);
            refreshTokenService.deleteByUserId(refreshToken.getUser().getId());
        } catch (Exception ignored) {
            // Token already invalid — still logout cleanly
        }
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    @PostMapping("/google")
    public ResponseEntity<?> googleLogin(@RequestBody Map<String, String> request) {
        try {
            String googleToken = request.get("token");

            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(), new GsonFactory()
            ).setAudience(Collections.singletonList(googleClientId)).build();

            GoogleIdToken idToken = verifier.verify(googleToken);

            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();
                String email = payload.getEmail();
                String name = (String) payload.get("name");

                String jwtToken = authService.googleLogin(email, name);
                RefreshToken refreshToken = refreshTokenService.createRefreshToken(email);

                User user = userRepository.findByEmail(email)
                        .orElseThrow(() -> new RuntimeException("User not found"));

                return ResponseEntity.ok(Map.of(
                        "accessToken", jwtToken,
                        "refreshToken", refreshToken.getToken(),
                        "email", user.getEmail(),
                        "firstName", user.getFirstName(),
                        "lastName", user.getLastName(),
                        "role", user.getRole() != null ? user.getRole().getName() : "STUDENT"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of("message", "Invalid Google token"));
            }

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Google login failed: " + e.getMessage()));
        }
    }
}