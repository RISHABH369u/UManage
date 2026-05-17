package com.umanage.enrollment.service;

import com.umanage.common.exception.UnauthorizedException;
import com.umanage.enrollment.entity.EnrollmentRecord;
import com.umanage.enrollment.entity.EnrollmentRequest;
import com.umanage.enrollment.entity.EnrollmentRequestStatus;
import com.umanage.enrollment.entity.EnrollmentStatus;
import com.umanage.enrollment.repository.EnrollmentRecordRepository;
import com.umanage.enrollment.repository.EnrollmentRequestRepository;
import com.umanage.security.AuthUserPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class EnrollmentQueryService {

    private final EnrollmentRequestRepository enrollmentRequestRepository;
    private final EnrollmentRecordRepository enrollmentRecordRepository;

    public EnrollmentQueryService(EnrollmentRequestRepository enrollmentRequestRepository,
                                  EnrollmentRecordRepository enrollmentRecordRepository) {
        this.enrollmentRequestRepository = enrollmentRequestRepository;
        this.enrollmentRecordRepository = enrollmentRecordRepository;
    }

    @Transactional(readOnly = true)
    public List<EnrollmentRequest> listMyRequestHistory() {
        return enrollmentRequestRepository.findByStudentUserIdOrderByCreatedAtDesc(getActorUserIdRequired());
    }

    @Transactional(readOnly = true)
    public List<EnrollmentRecord> listMyActiveRegistrations() {
        return enrollmentRecordRepository.findByStudentUserIdAndStatusOrderByUpdatedAtDesc(
                getActorUserIdRequired(),
                EnrollmentStatus.ACTIVE
        );
    }

    @Transactional(readOnly = true)
    public List<EnrollmentRequest> listPendingRequests() {
        return enrollmentRequestRepository.findByStatusOrderByCreatedAtAsc(EnrollmentRequestStatus.PENDING);
    }

    private UUID getActorUserIdRequired() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AuthUserPrincipal principal) {
            return principal.getUserId();
        }
        throw new UnauthorizedException("Authentication required");
    }
}
