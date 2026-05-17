package com.umanage.enrollment.mapper;

import com.umanage.enrollment.dto.EnrollmentRecordResponse;
import com.umanage.enrollment.dto.EnrollmentRequestResponse;
import com.umanage.enrollment.entity.EnrollmentRecord;
import com.umanage.enrollment.entity.EnrollmentRequest;
import org.springframework.stereotype.Component;

@Component
public class EnrollmentMapper {

    public EnrollmentRequestResponse toRequestResponse(EnrollmentRequest request) {
        return new EnrollmentRequestResponse(
                request.getId(),
                request.getStudentUser().getId(),
                request.getCourseOffering().getId(),
                request.getCourseOffering().getCourse().getCode(),
                request.getCourseOffering().getCourse().getTitle(),
                request.getCourseOffering().getSemester().getName(),
                request.getCourseOffering().getSectionCode(),
                request.getRequestedAction(),
                request.getStatus(),
                request.getDecidedByUser() != null ? request.getDecidedByUser().getId() : null,
                request.getDecisionReason(),
                request.getCreatedAt(),
                request.getUpdatedAt()
        );
    }

    public EnrollmentRecordResponse toRecordResponse(EnrollmentRecord record) {
        return new EnrollmentRecordResponse(
                record.getId(),
                record.getCourseOffering().getId(),
                record.getCourseOffering().getCourse().getCode(),
                record.getCourseOffering().getCourse().getTitle(),
                record.getCourseOffering().getSemester().getName(),
                record.getCourseOffering().getSectionCode(),
                record.getStatus(),
                record.getUpdatedAt()
        );
    }
}
