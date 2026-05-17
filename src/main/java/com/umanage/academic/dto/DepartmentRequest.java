package com.umanage.academic.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record DepartmentRequest(
        @NotBlank(message = "Department code is required")
        @Size(max = 20, message = "Department code must be at most 20 chars")
        @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "Department code contains invalid characters")
        String code,

        @NotBlank(message = "Department name is required")
        @Size(max = 120, message = "Department name must be at most 120 chars")
        String name
) {
}
