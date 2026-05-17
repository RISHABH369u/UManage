package com.umanage.enrollment.dto;

import com.umanage.enrollment.entity.EnrollmentStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record EnrollmentRecordResponse(
        UUID id,
        UUID courseOfferingId,
        String courseCode,
        String courseTitle,
        String semesterName,
        String sectionCode,
        EnrollmentStatus status,
        OffsetDateTime updatedAt
) {
}
