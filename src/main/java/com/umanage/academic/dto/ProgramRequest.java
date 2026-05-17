package com.umanage.academic.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record ProgramRequest(
        @NotBlank(message = "Program code is required")
        @Size(max = 20, message = "Program code must be at most 20 chars")
        @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "Program code contains invalid characters")
        String code,

        @NotBlank(message = "Program name is required")
        @Size(max = 150, message = "Program name must be at most 150 chars")
        String name,

        @NotNull(message = "Department id is required")
        UUID departmentId
) {
}
