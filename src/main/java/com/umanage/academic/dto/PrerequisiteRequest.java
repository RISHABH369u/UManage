package com.umanage.academic.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record PrerequisiteRequest(
        @NotNull(message = "Course id is required")
        UUID courseId,

        @NotNull(message = "Prerequisite course id is required")
        UUID prerequisiteCourseId
) {
}
