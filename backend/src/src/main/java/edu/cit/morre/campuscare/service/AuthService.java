package edu.cit.morre.campuscare.service;

import edu.cit.morre.campuscare.dto.AuthResponse;
import edu.cit.morre.campuscare.model.User;
import edu.cit.morre.campuscare.model.UserFactory;
import edu.cit.morre.campuscare.repository.UserRepository;
import edu.cit.morre.campuscare.strategy.AuthStrategy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthResponse register(String name, String email, String password) {

        if (name == null || email == null || password == null ||
                name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            throw new RuntimeException("All fields are required");
        }

        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already registered");
        }

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));

        userRepository.save(user);

        return new AuthResponse("registered-success");
    }

    public AuthResponse login(String email, String password) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        String token = "jwt-token-" + user.getEmail(); // temporary

        return new AuthResponse(token);
    }

    public Object authenticate(AuthStrategy strategy) {
        return strategy.authenticate();
    }

    // ⭐ Google OAuth login support
    public AuthResponse authenticateWithGoogleOAuth2User(OAuth2User oAuth2User) {

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        if (email == null) {
            throw new RuntimeException("Google account email not found");
        }

        // check if user already exists
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            user = UserFactory.createOAuthUser(name, email, passwordEncoder);

            userRepository.save(user);
        }

        // later you can generate JWT token here
        String token = "google-oauth-token";

        return new AuthResponse(token);
    }
    public String googleLogin(String email, String name) {

        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            user = new User();
            user.setEmail(email);
            user.setName(name != null ? name : "Google User");
            user.setPassword(passwordEncoder.encode("oauth-user"));

            userRepository.save(user);
        }

        // 🔥 For now return fake JWT (later replace with real JWT)
        return "jwt-token-" + user.getEmail();
    }
}