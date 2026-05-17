package com.umanage.enrollment.repository;

import com.umanage.enrollment.entity.EnrollmentAction;
import com.umanage.enrollment.entity.EnrollmentRequest;
import com.umanage.enrollment.entity.EnrollmentRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EnrollmentRequestRepository extends JpaRepository<EnrollmentRequest, UUID> {
    boolean existsByStudentUserIdAndCourseOfferingIdAndRequestedActionAndStatus(UUID studentUserId,
                                                                                 UUID courseOfferingId,
                                                                                 EnrollmentAction requestedAction,
                                                                                 EnrollmentRequestStatus status);

    Optional<EnrollmentRequest> findByIdAndStatus(UUID id, EnrollmentRequestStatus status);

    List<EnrollmentRequest> findByStudentUserIdOrderByCreatedAtDesc(UUID studentUserId);

    List<EnrollmentRequest> findByStatusOrderByCreatedAtAsc(EnrollmentRequestStatus status);
}
