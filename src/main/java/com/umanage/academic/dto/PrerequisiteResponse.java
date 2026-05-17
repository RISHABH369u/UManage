package com.umanage.academic.dto;

import java.util.UUID;

public record PrerequisiteResponse(
        UUID id,
        UUID courseId,
        String courseCode,
        String courseTitle,
        UUID prerequisiteCourseId,
        String prerequisiteCourseCode,
        String prerequisiteCourseTitle
) {
}
