package edu.cit.morre.campuscare.features.profile;

import edu.cit.morre.campuscare.features.auth.AuthService;
import edu.cit.morre.campuscare.features.profile.dto.ProfileResponse;
import edu.cit.morre.campuscare.features.profile.dto.UpdateProfileRequest;
import edu.cit.morre.campuscare.shared.model.Role;
import edu.cit.morre.campuscare.shared.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ProfileServiceTest {

    @Autowired
    private ProfileService profileService;

    @Autowired
    private AuthService authService;

    @Autowired
    private RoleRepository roleRepository;

    @BeforeEach
    void setUp() {
        if (roleRepository.findByName("STUDENT").isEmpty()) {
            Role studentRole = new Role();
            studentRole.setName("STUDENT");
            roleRepository.save(studentRole);
        }
        // Create a test user
        try {
            authService.register("Profile", "Test", "profile@example.com", "Password123!");
        } catch (Exception e) {
            // User may already exist
        }
    }

    @Test
    void testGetProfile_Success() {
        ProfileResponse profile = profileService.getProfile("profile@example.com");

        assertNotNull(profile);
        assertEquals("Profile", profile.getFirstName());
        assertEquals("Test", profile.getLastName());
        assertEquals("profile@example.com", profile.getEmail());
        assertEquals("STUDENT", profile.getRole());
    }

    @Test
    void testUpdateProfile_Success() {
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setFirstName("Updated");
        request.setLastName("Name");

        profileService.updateProfile("profile@example.com", request);

        ProfileResponse profile = profileService.getProfile("profile@example.com");
        assertEquals("Updated", profile.getFirstName());
        assertEquals("Name", profile.getLastName());
    }

    @Test
    void testChangePassword_Success() {
        profileService.changePassword("profile@example.com", "Password123!", "NewPass123!");

        // Verify can login with new password
        assertDoesNotThrow(() -> {
            authService.login("profile@example.com", "NewPass123!");
        });
    }

    @Test
    void testChangePassword_WrongCurrent_ThrowsException() {
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> profileService.changePassword("profile@example.com", "WrongCurrent", "NewPass123!")
        );
        assertEquals("Current password is incorrect", exception.getMessage());
    }

    @Test
    void testGetProfile_NonExistentUser_ThrowsException() {
        assertThrows(RuntimeException.class, () -> {
            profileService.getProfile("nonexistent@example.com");
        });
    }
}