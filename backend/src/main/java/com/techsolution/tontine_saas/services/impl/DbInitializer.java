package com.techsolution.tontine_saas.services.impl;

import com.techsolution.tontine_saas.entities.Association;
import com.techsolution.tontine_saas.entities.Role;
import com.techsolution.tontine_saas.entities.User;
import com.techsolution.tontine_saas.repository.AssociationRepository;
import com.techsolution.tontine_saas.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
@RequiredArgsConstructor
public class DbInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final AssociationRepository associationRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        if (!userRepository.existsByEmail("admin@tontine-saas.com")) {

            // Création de l'association racine
            Association adminAssoc = associationRepository
                    .findByNameIgnoreCase("PLATEFORME_ADMIN")
                    .orElseGet(() -> {
                        Association assoc = new Association();
                        assoc.setName("PLATEFORME_ADMIN");
                        assoc.setActive(true);
                        return associationRepository.save(assoc);
                    });

            // Création du premier Super Admin
            User superAdmin = new User();
            superAdmin.setFirstName("Super");
            superAdmin.setLastName("Admin");
            superAdmin.setEmail("admin@tontine-saas.com");
            superAdmin.setPassword(passwordEncoder.encode("admin@123"));
            superAdmin.setRoles(Collections.singletonList(Role.SUPER_ADMIN));
            superAdmin.setAssociation(adminAssoc);
            superAdmin.setActive(true);

            userRepository.save(superAdmin);

            System.out.println(">>> SUPER ADMIN créé : admin@tontine-saas.com / admin@123");
        }
    }

}
