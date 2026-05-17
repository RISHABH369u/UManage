package com.umanage.academic.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record FacultyAssignmentRequest(
        @NotNull(message = "Course offering id is required")
        UUID courseOfferingId,

        @NotNull(message = "Faculty user id is required")
        UUID facultyUserId
) {
}
