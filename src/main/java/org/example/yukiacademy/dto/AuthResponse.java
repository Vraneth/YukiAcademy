package org.example.yukiacademy.dto;

import lombok.Data;
import java.util.List;

@Data
public class AuthResponse {
    private String token; // El JWT
    private String type = "Bearer";
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private List<String> roles;
    private String profilePictureUrl;
    private String bio;


    public AuthResponse(String accessToken, Long id, String email, String firstName, String lastName, List<String> roles, String profilePictureUrl, String bio) {
        this.token = accessToken;
        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.roles = roles;
        this.profilePictureUrl = profilePictureUrl;
        this.bio = bio;
    }
}
