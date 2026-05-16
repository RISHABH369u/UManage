package com.umanage.users.dto;

import java.util.Set;
import java.util.UUID;

public record RoleResponse(
        UUID id,
        String name,
        String description,
        Set<String> permissions
) {
}
