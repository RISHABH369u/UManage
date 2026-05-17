package com.umanage.enrollment.dto;

import jakarta.validation.constraints.Size;

public record EnrollmentDecisionRequest(
        @Size(max = 500, message = "Reason must be at most 500 characters")
        String reason
) {
}
