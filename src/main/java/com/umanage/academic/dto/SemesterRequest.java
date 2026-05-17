package com.umanage.academic.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record SemesterRequest(
        @NotBlank(message = "Semester name is required")
        @Size(max = 120, message = "Semester name must be at most 120 chars")
        String name,

        @Min(value = 1, message = "Semester sequence number must be at least 1")
        @Max(value = 30, message = "Semester sequence number must be at most 30")
        int sequenceNumber,

        @NotNull(message = "Program id is required")
        UUID programId
) {
}
