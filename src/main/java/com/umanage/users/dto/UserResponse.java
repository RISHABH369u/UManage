package com.umanage.users.dto;

import com.umanage.users.entity.UserStatus;

import java.util.Set;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        String firstName,
        String lastName,
        UserStatus status,
        boolean enabled,
        Set<String> roles
) {
}
