package com.umanage.users.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record CreateUserRequest(
        @Email(message = "Valid email is required")
        @NotBlank(message = "Email is required")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 72, message = "Password must be between 8 and 72 characters")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d]).+$",
                message = "Password must include uppercase, lowercase, number, and special character"
        )
        String password,

        @NotBlank(message = "First name is required")
        @Size(max = 100, message = "First name must be at most 100 chars")
        @Pattern(regexp = "^[\\p{L} .'-]+$", message = "First name contains invalid characters")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(max = 100, message = "Last name must be at most 100 chars")
        @Pattern(regexp = "^[\\p{L} .'-]+$", message = "Last name contains invalid characters")
        String lastName,

        @NotEmpty(message = "At least one role is required")
        Set<String> roles
) {
}
