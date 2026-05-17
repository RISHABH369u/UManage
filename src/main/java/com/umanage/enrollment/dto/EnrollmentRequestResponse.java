package com.umanage.enrollment.dto;

import com.umanage.enrollment.entity.EnrollmentAction;
import com.umanage.enrollment.entity.EnrollmentRequestStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record EnrollmentRequestResponse(
        UUID id,
        UUID studentUserId,
        UUID courseOfferingId,
        String courseCode,
        String courseTitle,
        String semesterName,
        String sectionCode,
        EnrollmentAction requestedAction,
        EnrollmentRequestStatus status,
        UUID decidedByUserId,
        String decisionReason,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
