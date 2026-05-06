package edu.cit.morre.campuscare.features.auth;

import edu.cit.morre.campuscare.shared.exception.AuthenticationException;
import edu.cit.morre.campuscare.features.email.EmailService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import edu.cit.morre.campuscare.features.auth.dto.AuthResponse;
import edu.cit.morre.campuscare.shared.model.Role;
import edu.cit.morre.campuscare.shared.model.User;
import edu.cit.morre.campuscare.shared.repository.RoleRepository;
import edu.cit.morre.campuscare.shared.repository.UserRepository;
import edu.cit.morre.campuscare.shared.util.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;

    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       EmailService emailService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.emailService = emailService;
    }

    public AuthResponse authenticateWithGoogleOAuth2User(OAuth2User oAuth2User) {
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        if (email == null) {
            throw new RuntimeException("Google account email not found");
        }

        User user = userRepository.findByEmail(email).orElse(null);
        boolean isNewUser = user == null;

        if (isNewUser) {
            Role role = roleRepository.findByName("STUDENT")
                    .orElseThrow(() -> new RuntimeException("STUDENT role not found"));

            String[] parts = name != null ? name.split(" ", 2) : new String[]{"OAuth", "User"};

            user = new User();
            user.setFirstName(parts[0]);
            user.setLastName(parts.length > 1 ? parts[1] : "");
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode("oauth2-user"));
            user.setProvider("google");
            user.setRole(role);
            userRepository.save(user);

            // ✅ Send welcome email for new Google users
            emailService.sendWelcomeEmail(email, user.getFirstName());
        }

        String token = jwtUtil.generateToken(user);

        return new AuthResponse(
                token,
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole() != null ? user.getRole().getName() : "STUDENT"
        );
    }

    public AuthResponse register(String firstName, String lastName, String email, String password) {
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already registered");
        }

        Role role = roleRepository.findByName("STUDENT")
                .orElseThrow(() -> new RuntimeException("STUDENT role not found. Please seed roles table."));

        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setProvider("local");
        user.setRole(role);

        userRepository.save(user);

        // ✅ Send welcome email on registration
        emailService.sendWelcomeEmail(email, firstName);

        String token = jwtUtil.generateToken(user);

        return new AuthResponse(
                token,
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole().getName()
        );
    }

    public AuthResponse login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthenticationException("Invalid credentials"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new AuthenticationException("Invalid credentials");
        }

        String token = jwtUtil.generateToken(user);

        return new AuthResponse(
                token,
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole() != null ? user.getRole().getName() : "STUDENT"
        );
    }

    public String googleLogin(String email, String name) {
        User user = userRepository.findByEmail(email).orElse(null);
        boolean isNewUser = user == null;

        if (isNewUser) {
            Role role = roleRepository.findByName("STUDENT")
                    .orElseThrow(() -> new RuntimeException("STUDENT role not found"));

            String[] parts = name != null ? name.split(" ", 2) : new String[]{"Google", "User"};

            user = new User();
            user.setFirstName(parts[0]);
            user.setLastName(parts.length > 1 ? parts[1] : "");
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode("oauth-user"));
            user.setProvider("google");
            user.setRole(role);
            userRepository.save(user);

            // ✅ Send welcome email for new Google users
            emailService.sendWelcomeEmail(email, user.getFirstName());
        }

        return jwtUtil.generateToken(user);
    }
}