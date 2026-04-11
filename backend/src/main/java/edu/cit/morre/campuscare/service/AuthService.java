package edu.cit.morre.campuscare.service;

import org.springframework.security.oauth2.core.user.OAuth2User;
import edu.cit.morre.campuscare.dto.AuthResponse;
import edu.cit.morre.campuscare.model.Role;
import edu.cit.morre.campuscare.model.User;
import edu.cit.morre.campuscare.repository.RoleRepository;
import edu.cit.morre.campuscare.repository.UserRepository;
import edu.cit.morre.campuscare.util.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }
    public AuthResponse authenticateWithGoogleOAuth2User(OAuth2User oAuth2User) {
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        if (email == null) {
            throw new RuntimeException("Google account email not found");
        }

        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            Role role = roleRepository.findByName("STUDENT").orElse(null);
            String[] parts = name != null ? name.split(" ", 2) : new String[]{"OAuth", "User"};

            user = new User();
            user.setFirstName(parts[0]);
            user.setLastName(parts.length > 1 ? parts[1] : "");
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode("oauth2-user"));
            user.setProvider("google");
            user.setRole(role);
            userRepository.save(user);
        }

        return new AuthResponse(jwtUtil.generateToken(email));
    }

    public AuthResponse register(String firstName, String lastName, String email, String password) {
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already registered");
        }

        // Default role = STUDENT
        Role role = roleRepository.findByName("STUDENT")
                .orElseThrow(() -> new RuntimeException("Default role not found. Please seed roles table."));

        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setProvider("local");
        user.setRole(role);

        userRepository.save(user);
        return new AuthResponse(jwtUtil.generateToken(email));
    }

    public AuthResponse login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        return new AuthResponse(jwtUtil.generateToken(email));
    }

    public String googleLogin(String email, String name) {
        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            Role role = roleRepository.findByName("STUDENT").orElse(null);
            String[] parts = name != null ? name.split(" ", 2) : new String[]{"Google", "User"};

            user = new User();
            user.setFirstName(parts[0]);
            user.setLastName(parts.length > 1 ? parts[1] : "");
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode("oauth-user"));
            user.setProvider("google");
            user.setRole(role);
            userRepository.save(user);
        }

        return jwtUtil.generateToken(email);
    }
}