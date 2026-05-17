package com.umanage.enrollment.service;

import com.umanage.academic.entity.Course;
import com.umanage.academic.entity.CourseOffering;
import com.umanage.academic.repository.CourseOfferingRepository;
import com.umanage.audit.service.AuditLogService;
import com.umanage.common.exception.ConflictException;
import com.umanage.enrollment.dto.CreateEnrollmentRequest;
import com.umanage.enrollment.entity.EnrollmentAction;
import com.umanage.enrollment.entity.EnrollmentRecord;
import com.umanage.enrollment.entity.EnrollmentRequest;
import com.umanage.enrollment.entity.EnrollmentRequestStatus;
import com.umanage.enrollment.entity.EnrollmentStatus;
import com.umanage.enrollment.repository.EnrollmentRecordRepository;
import com.umanage.enrollment.repository.EnrollmentRequestRepository;
import com.umanage.security.AuthUserPrincipal;
import com.umanage.users.entity.User;
import com.umanage.users.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnrollmentWorkflowServiceTest {

    @Mock
    private EnrollmentRequestRepository enrollmentRequestRepository;
    @Mock
    private EnrollmentRecordRepository enrollmentRecordRepository;
    @Mock
    private CourseOfferingRepository courseOfferingRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AuditLogService auditLogService;
    @Mock
    private HttpServletRequest httpServletRequest;

    private EnrollmentWorkflowService enrollmentWorkflowService;

    @BeforeEach
    void setUp() {
        enrollmentWorkflowService = new EnrollmentWorkflowService(
                enrollmentRequestRepository,
                enrollmentRecordRepository,
                courseOfferingRepository,
                userRepository,
                auditLogService
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void submitAddRequestShouldPreventDuplicatePendingRequest() {
        UUID studentId = UUID.randomUUID();
        UUID offeringId = UUID.randomUUID();
        setAuthenticatedUser(studentId);

        var student = new User();
        student.setId(studentId);
        when(userRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(courseOfferingRepository.findById(offeringId)).thenReturn(Optional.of(buildOffering(offeringId, 1)));
        when(enrollmentRecordRepository.existsByStudentUserIdAndCourseOfferingIdAndStatus(studentId, offeringId, EnrollmentStatus.ACTIVE))
                .thenReturn(false);
        when(enrollmentRequestRepository.existsByStudentUserIdAndCourseOfferingIdAndRequestedActionAndStatus(
                studentId,
                offeringId,
                EnrollmentAction.ADD,
                EnrollmentRequestStatus.PENDING
        )).thenReturn(true);

        assertThatThrownBy(() -> enrollmentWorkflowService.submitRequest(
                new CreateEnrollmentRequest(offeringId, EnrollmentAction.ADD),
                httpServletRequest
        )).isInstanceOf(ConflictException.class)
                .hasMessageContaining("pending add request already exists");
    }

    @Test
    void approveAddShouldFailWhenSeatLimitReached() {
        UUID approverId = UUID.randomUUID();
        UUID requestId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();
        UUID offeringId = UUID.randomUUID();
        setAuthenticatedUser(approverId);

        var approver = new User();
        approver.setId(approverId);
        when(userRepository.findById(approverId)).thenReturn(Optional.of(approver));

        var pending = new EnrollmentRequest();
        pending.setId(requestId);
        var student = new User();
        student.setId(studentId);
        pending.setStudentUser(student);
        pending.setCourseOffering(buildOffering(offeringId, 1));
        pending.setRequestedAction(EnrollmentAction.ADD);
        pending.setStatus(EnrollmentRequestStatus.PENDING);

        when(enrollmentRequestRepository.findByIdAndStatus(requestId, EnrollmentRequestStatus.PENDING)).thenReturn(Optional.of(pending));
        when(enrollmentRecordRepository.countByCourseOfferingIdAndStatus(offeringId, EnrollmentStatus.ACTIVE)).thenReturn(1L);

        assertThatThrownBy(() -> enrollmentWorkflowService.approveRequest(requestId, null, httpServletRequest))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Seat limit reached");

        verify(enrollmentRecordRepository, never()).save(any(EnrollmentRecord.class));
    }

    @Test
    void approveAddShouldReactivateDroppedEnrollment() {
        UUID approverId = UUID.randomUUID();
        UUID requestId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();
        UUID offeringId = UUID.randomUUID();
        setAuthenticatedUser(approverId);

        var approver = new User();
        approver.setId(approverId);
        when(userRepository.findById(approverId)).thenReturn(Optional.of(approver));

        var pending = new EnrollmentRequest();
        pending.setId(requestId);
        var student = new User();
        student.setId(studentId);
        pending.setStudentUser(student);
        pending.setCourseOffering(buildOffering(offeringId, 2));
        pending.setRequestedAction(EnrollmentAction.ADD);
        pending.setStatus(EnrollmentRequestStatus.PENDING);

        var existingRecord = new EnrollmentRecord();
        existingRecord.setStatus(EnrollmentStatus.DROPPED);
        existingRecord.setStudentUser(student);
        existingRecord.setCourseOffering(pending.getCourseOffering());

        when(enrollmentRequestRepository.findByIdAndStatus(requestId, EnrollmentRequestStatus.PENDING)).thenReturn(Optional.of(pending));
        when(enrollmentRecordRepository.countByCourseOfferingIdAndStatus(offeringId, EnrollmentStatus.ACTIVE)).thenReturn(1L);
        when(enrollmentRecordRepository.findByStudentUserIdAndCourseOfferingId(studentId, offeringId)).thenReturn(Optional.of(existingRecord));
        when(enrollmentRequestRepository.save(any(EnrollmentRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(httpServletRequest.getRemoteAddr()).thenReturn("127.0.0.1");

        var approved = enrollmentWorkflowService.approveRequest(requestId, "ok", httpServletRequest);

        assertThat(existingRecord.getStatus()).isEqualTo(EnrollmentStatus.ACTIVE);
        assertThat(approved.getStatus()).isEqualTo(EnrollmentRequestStatus.APPROVED);
        assertThat(approved.getDecisionReason()).isEqualTo("ok");
    }

    @Test
    void approveDropShouldMarkEnrollmentDropped() {
        UUID approverId = UUID.randomUUID();
        UUID requestId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();
        UUID offeringId = UUID.randomUUID();
        setAuthenticatedUser(approverId);

        var approver = new User();
        approver.setId(approverId);
        when(userRepository.findById(approverId)).thenReturn(Optional.of(approver));

        var pending = new EnrollmentRequest();
        pending.setId(requestId);
        var student = new User();
        student.setId(studentId);
        pending.setStudentUser(student);
        pending.setCourseOffering(buildOffering(offeringId, 3));
        pending.setRequestedAction(EnrollmentAction.DROP);
        pending.setStatus(EnrollmentRequestStatus.PENDING);

        var activeRecord = new EnrollmentRecord();
        activeRecord.setStatus(EnrollmentStatus.ACTIVE);
        activeRecord.setStudentUser(student);
        activeRecord.setCourseOffering(pending.getCourseOffering());

        when(enrollmentRequestRepository.findByIdAndStatus(requestId, EnrollmentRequestStatus.PENDING)).thenReturn(Optional.of(pending));
        when(enrollmentRecordRepository.findByStudentUserIdAndCourseOfferingId(studentId, offeringId)).thenReturn(Optional.of(activeRecord));
        when(enrollmentRequestRepository.save(any(EnrollmentRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(httpServletRequest.getRemoteAddr()).thenReturn("127.0.0.1");

        var approved = enrollmentWorkflowService.approveRequest(requestId, null, httpServletRequest);

        ArgumentCaptor<EnrollmentRecord> recordCaptor = ArgumentCaptor.forClass(EnrollmentRecord.class);
        verify(enrollmentRecordRepository).save(recordCaptor.capture());
        assertThat(recordCaptor.getValue().getStatus()).isEqualTo(EnrollmentStatus.DROPPED);
        assertThat(approved.getStatus()).isEqualTo(EnrollmentRequestStatus.APPROVED);
    }

    private void setAuthenticatedUser(UUID userId) {
        var principal = new AuthUserPrincipal(userId, "user@umanage.local", "pwd", true, List.of());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );
    }

    private CourseOffering buildOffering(UUID offeringId, int capacity) {
        var course = new Course();
        course.setCode("CS101");
        course.setTitle("Intro");

        var offering = new CourseOffering();
        offering.setId(offeringId);
        offering.setCourse(course);
        offering.setCapacity(capacity);
        offering.setSectionCode("A");
        var semester = new com.umanage.academic.entity.Semester();
        semester.setName("Semester 1");
        offering.setSemester(semester);
        return offering;
    }
}
