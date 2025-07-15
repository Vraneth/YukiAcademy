package org.example.yukiacademy.dto;

import lombok.Data;
import java.util.Set;

@Data
public class UserProfileDto {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String profilePictureUrl;
    private String bio;
    private String interests;
    private Boolean profileVisibleToPublic;
    private Boolean receiveEmailNotifications;
    private Set<String> roles;
}