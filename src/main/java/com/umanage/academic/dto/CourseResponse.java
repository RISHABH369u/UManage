package com.umanage.academic.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CourseResponse(
        UUID id,
        String code,
        String title,
        BigDecimal credits,
        UUID departmentId,
        String departmentCode,
        String departmentName
) {
}
