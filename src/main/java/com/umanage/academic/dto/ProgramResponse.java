package com.umanage.academic.dto;

import java.util.UUID;

public record ProgramResponse(
        UUID id,
        String code,
        String name,
        UUID departmentId,
        String departmentCode,
        String departmentName
) {
}
