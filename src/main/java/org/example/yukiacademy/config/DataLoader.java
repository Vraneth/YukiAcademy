package org.example.yukiacademy.config; // O un paquete util o initialization

import org.example.yukiacademy.model.Role;
import org.example.yukiacademy.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class DataLoader {

    @Bean
    CommandLineRunner initRoles(RoleRepository roleRepository) {
        return args -> {
            Arrays.stream(Role.RoleName.values()).forEach(roleName -> {
                if (roleRepository.findByName(roleName).isEmpty()) {
                    roleRepository.save(new Role(roleName));
                }
            });
        };
    }
}