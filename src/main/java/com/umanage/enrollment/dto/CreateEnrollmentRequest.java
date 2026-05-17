package com.umanage.enrollment.dto;

import com.umanage.enrollment.entity.EnrollmentAction;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateEnrollmentRequest(
        @NotNull(message = "Course offering id is required")
        UUID courseOfferingId,

        @NotNull(message = "Action is required")
        EnrollmentAction action
) {
}
