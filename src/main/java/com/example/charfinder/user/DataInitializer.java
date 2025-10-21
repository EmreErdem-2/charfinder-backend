package com.example.charfinder.user;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    @Value("${initial.admin.user}") private String adminUser;
    @Value("${initial.admin.password}") private String adminPass;

    @Override
    public void run(String... args) {
        // Ensure roles exist
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setName("ROLE_USER");
                    return roleRepository.save(r);
                });

        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setName("ROLE_ADMIN");
                    return roleRepository.save(r);
                });

        // Ensure default admin user exists
        userRepository.findByEmail(adminUser).orElseGet(() -> {
            User admin = new User();
            admin.setEmail(adminUser);
            admin.setPasswordHash(passwordEncoder.encode(adminPass));
            admin.setEnabled(true);
            admin.setRoles(Set.of(userRole, adminRole));
            return userRepository.save(admin);
        });
    }
}