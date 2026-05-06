package edu.cit.morre.campuscare.features.auth;

import edu.cit.morre.campuscare.features.auth.dto.AuthResponse;
import edu.cit.morre.campuscare.shared.exception.AuthenticationException;
import edu.cit.morre.campuscare.shared.model.Role;
import edu.cit.morre.campuscare.shared.repository.RoleRepository;
import edu.cit.morre.campuscare.shared.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private RoleRepository roleRepository;

    // Unique timestamp to avoid email conflicts across test classes
    private final String timestamp = String.valueOf(System.currentTimeMillis());

    @BeforeEach
    void setUp() {
        if (roleRepository.findByName("STUDENT").isEmpty()) {
            Role studentRole = new Role();
            studentRole.setName("STUDENT");
            roleRepository.save(studentRole);
        }
    }

    @Test
    void testRegisterNewUser_Success() {
        String email = "test" + timestamp + "@example.com";

        AuthResponse response = authService.register(
                "Test", "User", email, "Password123!"
        );

        assertNotNull(response);
        assertNotNull(response.getToken());
        assertEquals(email, response.getEmail());
        assertEquals("Test", response.getFirstName());
        assertEquals("User", response.getLastName());
        assertEquals("STUDENT", response.getRole());
    }

    @Test
    void testRegisterDuplicateEmail_ThrowsException() {
        String email = "duplicate" + timestamp + "@example.com";

        // Register first user
        authService.register("Test", "User", email, "Password123!");

        // Try registering with same email
        assertThrows(RuntimeException.class, () -> {
            authService.register("Another", "User", email, "Password123!");
        });
    }

    @Test
    void testLoginWithValidCredentials_Success() {
        String email = "login" + timestamp + "@example.com";
        authService.register("Login", "Test", email, "Password123!");

        AuthResponse response = authService.login(email, "Password123!");

        assertNotNull(response);
        assertNotNull(response.getToken());
        assertEquals(email, response.getEmail());
    }

    @Test
    void testLoginWithWrongPassword_ThrowsException() {
        String email = "wrong" + timestamp + "@example.com";
        authService.register("Wrong", "Pass", email, "Password123!");

        AuthenticationException exception = assertThrows(
                AuthenticationException.class,
                () -> authService.login(email, "WrongPassword")
        );
        assertEquals("Invalid credentials", exception.getMessage());
    }

    @Test
    void testLoginWithNonExistentEmail_ThrowsException() {
        AuthenticationException exception = assertThrows(
                AuthenticationException.class,
                () -> authService.login("nonexistent" + timestamp + "@example.com", "Password123!")
        );
        assertEquals("Invalid credentials", exception.getMessage());
    }
}