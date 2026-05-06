package edu.cit.morre.campuscare.features.profile;

import edu.cit.morre.campuscare.features.profile.dto.ProfileResponse;
import edu.cit.morre.campuscare.features.profile.dto.UpdateProfileRequest;
import edu.cit.morre.campuscare.shared.model.User;
import edu.cit.morre.campuscare.shared.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class ProfileService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public ProfileService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public ProfileResponse getProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return new ProfileResponse(
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getRole() != null ? user.getRole().getName() : "STUDENT"
        );
    }

    public void updateProfile(String email, UpdateProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.getFirstName() != null && !request.getFirstName().isEmpty()) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null && !request.getLastName().isEmpty()) {
            user.setLastName(request.getLastName());
        }

        userRepository.save(user);
    }
    // Add this method to your ProfileService
    public void changePassword(String email, String currentPassword, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        // Encode and save new password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}