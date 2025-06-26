package org.example.yukiacademy.dto;

import lombok.Data;
import java.util.List;

@Data // Lombok
public class AuthResponse {
    private String token; // El JWT
    private String type = "Bearer";
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private List<String> roles;

    public AuthResponse(String accessToken, Long id, String email, String firstName, String lastName, List<String> roles) {
        this.token = accessToken;
        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.roles = roles;
    }
}