package com.umanage.academic.dto;

import java.util.UUID;

public record CourseOfferingResponse(
        UUID id,
        UUID courseId,
        String courseCode,
        String courseTitle,
        UUID semesterId,
        String semesterName,
        int semesterSequenceNumber,
        String sectionCode,
        int capacity
) {
}
