package org.example.yukiacademy.service;

import org.example.yukiacademy.model.User;
import org.example.yukiacademy.repository.UserRepository;
import org.example.yukiacademy.security.details.UserDetailsImpl; // ¡Nueva importación!
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
// import org.springframework.security.core.GrantedAuthority; // Ya no necesario si UserDetailsImpl se encarga de esto
// import org.springframework.security.core.authority.SimpleGrantedAuthority; // Ya no necesario si UserDetailsImpl se encarga de esto

// import java.util.Collection; // Ya no necesario si UserDetailsImpl se encarga de esto
// import java.util.stream.Collectors; // Ya no necesario si UserDetailsImpl se encarga de esto

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con email: " + email));

        // ¡CAMBIO AQUÍ! Devuelve tu implementación personalizada de UserDetails
        return UserDetailsImpl.build(user);
    }
}