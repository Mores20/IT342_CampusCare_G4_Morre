package edu.cit.morre.campuscare.strategy;

import edu.cit.morre.campuscare.dto.AuthResponse;
import edu.cit.morre.campuscare.model.User;
import edu.cit.morre.campuscare.repository.UserRepository;
import edu.cit.morre.campuscare.model.UserFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;

public class OAuthStrategy implements AuthStrategy {

    private final OAuth2User oAuth2User;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public OAuthStrategy(OAuth2User oAuth2User,
                         UserRepository userRepository,
                         PasswordEncoder passwordEncoder) {
        this.oAuth2User = oAuth2User;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Object authenticate() {
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        if (email == null) {
            throw new RuntimeException("Google account email not found");
        }

        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            user = UserFactory.createOAuthUser(name, email, passwordEncoder);
            userRepository.save(user);
        }

        return new AuthResponse("google-oauth-token");
    }
}