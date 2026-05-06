package edu.cit.morre.campuscare.features.auth.strategy;

import edu.cit.morre.campuscare.shared.model.User;
import edu.cit.morre.campuscare.shared.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

public class LocalAuthStrategy implements AuthStrategy {

    private final String email;
    private final String password;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public LocalAuthStrategy(String email, String password,
                             UserRepository userRepository,
                             PasswordEncoder passwordEncoder) {
        this.email = email;
        this.password = password;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Object authenticate() {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        // Returns full name for confirmation
        return user.getFirstName() + " " + user.getLastName();
    }
}