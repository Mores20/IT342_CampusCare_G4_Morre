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

    }
}