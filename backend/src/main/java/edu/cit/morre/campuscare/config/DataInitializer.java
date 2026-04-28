package edu.cit.morre.campuscare.config;

import edu.cit.morre.campuscare.model.Role;
import edu.cit.morre.campuscare.model.User;
import edu.cit.morre.campuscare.repository.RoleRepository;
import edu.cit.morre.campuscare.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // Initialize Roles
        Role adminRole = roleRepository.findByName("ADMIN")
                .orElseGet(() -> {
                    Role newAdminRole = new Role();
                    newAdminRole.setName("ADMIN");
                    System.out.println("✅ Created ADMIN role");
                    return roleRepository.save(newAdminRole);
                });

        Role userRole = roleRepository.findByName("USER")
                .orElseGet(() -> {
                    Role newUserRole = new Role();
                    newUserRole.setName("USER");
                    System.out.println("✅ Created USER role");
                    return roleRepository.save(newUserRole);
                });

        // Also create STUDENT role for backward compatibility
        Role studentRole = roleRepository.findByName("STUDENT")
                .orElseGet(() -> {
                    Role newStudentRole = new Role();
                    newStudentRole.setName("STUDENT");
                    System.out.println("✅ Created STUDENT role (for compatibility)");
                    return roleRepository.save(newStudentRole);
                });

        // Create Admin User
        if (!userRepository.findByEmail("admin@campuscare.com").isPresent()) {
            User admin = new User();
            admin.setFirstName("Admin");
            admin.setLastName("User");
            admin.setEmail("admin@campuscare.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setProvider("local");
            admin.setRole(adminRole);

            userRepository.save(admin);
            System.out.println("✅ Admin user created!");
            System.out.println("   📧 Email: admin@campuscare.com");
            System.out.println("   🔑 Password: admin123");
            System.out.println("   👤 Role: ADMIN");
        } else {
            System.out.println("ℹ️ Admin user already exists");
        }

        // Create a regular test user (optional)
        if (!userRepository.findByEmail("user@campuscare.com").isPresent()) {
            User regularUser = new User();
            regularUser.setFirstName("Regular");
            regularUser.setLastName("User");
            regularUser.setEmail("user@campuscare.com");
            regularUser.setPassword(passwordEncoder.encode("user123"));
            regularUser.setProvider("local");
            regularUser.setRole(userRole);

            userRepository.save(regularUser);
            System.out.println("✅ Regular user created!");
            System.out.println("   📧 Email: user@campuscare.com");
            System.out.println("   🔑 Password: user123");
            System.out.println("   👤 Role: USER");
        }
    }
}