package com.umanage.users.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @NotBlank(message = "First name is required")
        @Size(max = 100, message = "First name must be at most 100 characters")
        @Pattern(regexp = "^[\\p{L} .'-]+$", message = "First name contains invalid characters")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(max = 100, message = "Last name must be at most 100 characters")
        @Pattern(regexp = "^[\\p{L} .'-]+$", message = "Last name contains invalid characters")
        String lastName
) {
}
