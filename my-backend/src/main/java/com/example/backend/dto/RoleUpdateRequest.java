package com.example.backend.dto;

import com.example.backend.domain.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RoleUpdateRequest {
    private User.Role role;
}