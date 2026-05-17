package com.umanage.enrollment.service;

import com.umanage.academic.repository.CourseOfferingRepository;
import com.umanage.audit.service.AuditLogService;
import com.umanage.common.exception.ConflictException;
import com.umanage.common.exception.ResourceNotFoundException;
import com.umanage.common.exception.UnauthorizedException;
import com.umanage.enrollment.dto.CreateEnrollmentRequest;
import com.umanage.enrollment.entity.EnrollmentAction;
import com.umanage.enrollment.entity.EnrollmentRecord;
import com.umanage.enrollment.entity.EnrollmentRequest;
import com.umanage.enrollment.entity.EnrollmentRequestStatus;
import com.umanage.enrollment.entity.EnrollmentStatus;
import com.umanage.enrollment.repository.EnrollmentRecordRepository;
import com.umanage.enrollment.repository.EnrollmentRequestRepository;
import com.umanage.security.AuthUserPrincipal;
import com.umanage.users.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class EnrollmentWorkflowService {

    private final EnrollmentRequestRepository enrollmentRequestRepository;
    private final EnrollmentRecordRepository enrollmentRecordRepository;
    private final CourseOfferingRepository courseOfferingRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    public EnrollmentWorkflowService(EnrollmentRequestRepository enrollmentRequestRepository,
                                     EnrollmentRecordRepository enrollmentRecordRepository,
                                     CourseOfferingRepository courseOfferingRepository,
                                     UserRepository userRepository,
                                     AuditLogService auditLogService) {
        this.enrollmentRequestRepository = enrollmentRequestRepository;
        this.enrollmentRecordRepository = enrollmentRecordRepository;
        this.courseOfferingRepository = courseOfferingRepository;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public EnrollmentRequest submitRequest(CreateEnrollmentRequest request, HttpServletRequest httpRequest) {
        var actorUserId = getActorUserIdRequired();
        var student = userRepository.findById(actorUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Student user not found"));
        var offering = courseOfferingRepository.findById(request.courseOfferingId())
                .orElseThrow(() -> new ResourceNotFoundException("Course offering not found"));

        validateRequestPreconditions(actorUserId, request);

        var enrollmentRequest = new EnrollmentRequest();
        enrollmentRequest.setStudentUser(student);
        enrollmentRequest.setCourseOffering(offering);
        enrollmentRequest.setRequestedAction(request.action());
        enrollmentRequest.setStatus(EnrollmentRequestStatus.PENDING);

        var saved = enrollmentRequestRepository.save(enrollmentRequest);
        auditLogService.log(
                actorUserId,
                "ENROLLMENT_REQUEST_" + request.action().name(),
                "EnrollmentRequest",
                saved.getId().toString(),
                "Submitted " + request.action().name() + " request",
                httpRequest.getRemoteAddr()
        );
        return saved;
    }

    @Transactional
    public EnrollmentRequest approveRequest(UUID requestId, String reason, HttpServletRequest httpRequest) {
        var actorUserId = getActorUserIdRequired();
        var approver = userRepository.findById(actorUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Approver user not found"));

        var enrollmentRequest = getPendingRequest(requestId);

        if (enrollmentRequest.getRequestedAction() == EnrollmentAction.ADD) {
            approveAdd(enrollmentRequest);
        } else {
            approveDrop(enrollmentRequest);
        }

        enrollmentRequest.setStatus(EnrollmentRequestStatus.APPROVED);
        enrollmentRequest.setDecidedByUser(approver);
        enrollmentRequest.setDecisionReason(normalizeReason(reason));

        var saved = enrollmentRequestRepository.save(enrollmentRequest);
        auditLogService.log(
                actorUserId,
                "ENROLLMENT_APPROVED",
                "EnrollmentRequest",
                saved.getId().toString(),
                "Approved " + enrollmentRequest.getRequestedAction().name() + " request",
                httpRequest.getRemoteAddr()
        );
        return saved;
    }

    @Transactional
    public EnrollmentRequest rejectRequest(UUID requestId, String reason, HttpServletRequest httpRequest) {
        var actorUserId = getActorUserIdRequired();
        var approver = userRepository.findById(actorUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Approver user not found"));

        var enrollmentRequest = getPendingRequest(requestId);
        enrollmentRequest.setStatus(EnrollmentRequestStatus.REJECTED);
        enrollmentRequest.setDecidedByUser(approver);
        enrollmentRequest.setDecisionReason(normalizeReason(reason));

        var saved = enrollmentRequestRepository.save(enrollmentRequest);
        auditLogService.log(
                actorUserId,
                "ENROLLMENT_REJECTED",
                "EnrollmentRequest",
                saved.getId().toString(),
                "Rejected " + enrollmentRequest.getRequestedAction().name() + " request",
                httpRequest.getRemoteAddr()
        );
        return saved;
    }

    private void validateRequestPreconditions(UUID studentUserId, CreateEnrollmentRequest request) {
        var offeringId = request.courseOfferingId();

        if (request.action() == EnrollmentAction.ADD) {
            if (enrollmentRecordRepository.existsByStudentUserIdAndCourseOfferingIdAndStatus(
                    studentUserId,
                    offeringId,
                    EnrollmentStatus.ACTIVE
            )) {
                throw new ConflictException("Student is already enrolled in this course offering");
            }
            if (enrollmentRequestRepository.existsByStudentUserIdAndCourseOfferingIdAndRequestedActionAndStatus(
                    studentUserId,
                    offeringId,
                    EnrollmentAction.ADD,
                    EnrollmentRequestStatus.PENDING
            )) {
                throw new ConflictException("A pending add request already exists for this course offering");
            }
            return;
        }

        var currentEnrollment = enrollmentRecordRepository.findByStudentUserIdAndCourseOfferingId(studentUserId, offeringId)
                .orElse(null);
        if (currentEnrollment == null || currentEnrollment.getStatus() != EnrollmentStatus.ACTIVE) {
            throw new ConflictException("Student is not actively enrolled in this course offering");
        }
        if (enrollmentRequestRepository.existsByStudentUserIdAndCourseOfferingIdAndRequestedActionAndStatus(
                studentUserId,
                offeringId,
                EnrollmentAction.DROP,
                EnrollmentRequestStatus.PENDING
        )) {
            throw new ConflictException("A pending drop request already exists for this course offering");
        }
    }

    private EnrollmentRequest getPendingRequest(UUID requestId) {
        return enrollmentRequestRepository.findByIdAndStatus(requestId, EnrollmentRequestStatus.PENDING)
                .orElseThrow(() -> new ResourceNotFoundException("Pending enrollment request not found"));
    }

    private void approveAdd(EnrollmentRequest enrollmentRequest) {
        var offering = enrollmentRequest.getCourseOffering();
        long activeSeats = enrollmentRecordRepository.countByCourseOfferingIdAndStatus(offering.getId(), EnrollmentStatus.ACTIVE);
        if (activeSeats >= offering.getCapacity()) {
            throw new ConflictException("Seat limit reached for this course offering");
        }

        var existing = enrollmentRecordRepository.findByStudentUserIdAndCourseOfferingId(
                enrollmentRequest.getStudentUser().getId(),
                offering.getId()
        ).orElse(null);

        if (existing == null) {
            var record = new EnrollmentRecord();
            record.setStudentUser(enrollmentRequest.getStudentUser());
            record.setCourseOffering(offering);
            record.setStatus(EnrollmentStatus.ACTIVE);
            enrollmentRecordRepository.save(record);
            return;
        }

        existing.setStatus(EnrollmentStatus.ACTIVE);
        enrollmentRecordRepository.save(existing);
    }

    private void approveDrop(EnrollmentRequest enrollmentRequest) {
        var existing = enrollmentRecordRepository.findByStudentUserIdAndCourseOfferingId(
                        enrollmentRequest.getStudentUser().getId(),
                        enrollmentRequest.getCourseOffering().getId())
                .orElseThrow(() -> new ConflictException("Student is not actively enrolled in this course offering"));

        if (existing.getStatus() != EnrollmentStatus.ACTIVE) {
            throw new ConflictException("Student is not actively enrolled in this course offering");
        }

        existing.setStatus(EnrollmentStatus.DROPPED);
        enrollmentRecordRepository.save(existing);
    }

    private UUID getActorUserIdRequired() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AuthUserPrincipal principal) {
            return principal.getUserId();
        }
        throw new UnauthorizedException("Authentication required");
    }

    private String normalizeReason(String reason) {
        if (reason == null || reason.isBlank()) {
            return null;
        }
        return reason.trim();
    }
}
