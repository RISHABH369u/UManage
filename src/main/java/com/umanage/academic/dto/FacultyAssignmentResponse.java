package com.umanage.academic.dto;

import java.util.UUID;

public record FacultyAssignmentResponse(
        UUID id,
        UUID courseOfferingId,
        String courseCode,
        String sectionCode,
        UUID facultyUserId,
        String facultyEmail,
        String facultyName
) {
}
