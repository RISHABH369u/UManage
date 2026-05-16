package com.umanage.users.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record CreateRoleRequest(
        @NotBlank(message = "Role name is required")
        @Size(max = 100, message = "Role name must be at most 100 chars")
        String name,

        @Size(max = 255, message = "Description must be at most 255 chars")
        String description,

        Set<String> permissionNames
) {
}
