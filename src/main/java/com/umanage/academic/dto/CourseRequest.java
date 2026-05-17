package com.umanage.academic.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record CourseRequest(
        @NotBlank(message = "Course code is required")
        @Size(max = 30, message = "Course code must be at most 30 chars")
        @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "Course code contains invalid characters")
        String code,

        @NotBlank(message = "Course title is required")
        @Size(max = 200, message = "Course title must be at most 200 chars")
        String title,

        @NotNull(message = "Credits is required")
        @DecimalMin(value = "0.5", message = "Credits must be at least 0.5")
        @DecimalMax(value = "30.0", message = "Credits must be at most 30.0")
        BigDecimal credits,

        @NotNull(message = "Department id is required")
        UUID departmentId
) {
}
