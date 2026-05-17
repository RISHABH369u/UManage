package com.umanage.academic.dto;

import java.util.UUID;

public record SemesterResponse(
        UUID id,
        String name,
        int sequenceNumber,
        UUID programId,
        String programCode,
        String programName
) {
}
