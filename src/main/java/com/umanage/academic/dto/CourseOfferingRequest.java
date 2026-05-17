package com.umanage.academic.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CourseOfferingRequest(
        @NotNull(message = "Course id is required")
        UUID courseId,

        @NotNull(message = "Semester id is required")
        UUID semesterId,

        @NotBlank(message = "Section code is required")
        @Size(max = 20, message = "Section code must be at most 20 chars")
        @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "Section code contains invalid characters")
        String sectionCode,

        @Min(value = 1, message = "Capacity must be at least 1")
        @Max(value = 2000, message = "Capacity must be at most 2000")
        int capacity
) {
}
