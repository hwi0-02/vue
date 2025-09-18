package com.example.backend.dto;

import com.example.backend.domain.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserAdminDto {
    private Long id;
    private String name;
    private String email;
    private User.Role role;
    private User.Status status;
    private User.Provider provider;
    private LocalDateTime createdAt;

    public static UserAdminDto from(User user) {
        return UserAdminDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .status(user.getStatus())
                .provider(user.getProvider())
                .createdAt(user.getCreatedAt())
                .build();
    }
}