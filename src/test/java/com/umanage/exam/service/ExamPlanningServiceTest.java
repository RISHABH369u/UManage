package com.umanage.exam.service;

import com.umanage.academic.entity.CourseOffering;
import com.umanage.academic.repository.CourseOfferingRepository;
import com.umanage.common.exception.ConflictException;
import com.umanage.enrollment.entity.EnrollmentRecord;
import com.umanage.enrollment.entity.EnrollmentStatus;
import com.umanage.enrollment.repository.EnrollmentRecordRepository;
import com.umanage.exam.entity.Exam;
import com.umanage.exam.entity.ExamInvigilatorAssignment;
import com.umanage.exam.entity.ExamRoomAllocation;
import com.umanage.exam.entity.ExamSeatPlan;
import com.umanage.exam.repository.ExamInvigilatorAssignmentRepository;
import com.umanage.exam.repository.ExamRepository;
import com.umanage.exam.repository.ExamRoomAllocationRepository;
import com.umanage.exam.repository.ExamSeatPlanRepository;
import com.umanage.users.entity.User;
import com.umanage.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExamPlanningServiceTest {

    @Mock
    private ExamRepository examRepository;
    @Mock
    private CourseOfferingRepository courseOfferingRepository;
    @Mock
    private EnrollmentRecordRepository enrollmentRecordRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ExamRoomAllocationRepository examRoomAllocationRepository;
    @Mock
    private ExamInvigilatorAssignmentRepository examInvigilatorAssignmentRepository;
    @Mock
    private ExamSeatPlanRepository examSeatPlanRepository;

    private ExamPlanningService examPlanningService;

    @BeforeEach
    void setUp() {
        examPlanningService = new ExamPlanningService(
                examRepository,
                courseOfferingRepository,
                enrollmentRecordRepository,
                userRepository,
                examRoomAllocationRepository,
                examInvigilatorAssignmentRepository,
                examSeatPlanRepository
        );
    }

    @Test
    void orchestrateShouldCreateDeterministicSeatPlanAndInvigilatorAssignment() {
        UUID examId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID offeringId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        UUID studentA = UUID.fromString("33333333-3333-3333-3333-333333333333");
        UUID studentB = UUID.fromString("44444444-4444-4444-4444-444444444444");
        UUID invigilatorA = UUID.fromString("55555555-5555-5555-5555-555555555555");
        UUID invigilatorB = UUID.fromString("66666666-6666-6666-6666-666666666666");

        var offering = new CourseOffering();
        offering.setId(offeringId);

        var exam = new Exam();
        exam.setId(examId);
        exam.setVersion(3);
        exam.setCourseOffering(offering);

        when(examRepository.findById(examId)).thenReturn(Optional.of(exam));
        when(examRepository.save(exam)).thenReturn(exam);

        when(enrollmentRecordRepository.findByCourseOfferingIdAndStatusOrderByStudentUserIdAsc(offeringId, EnrollmentStatus.ACTIVE))
                .thenReturn(List.of(buildEnrollment(studentB), buildEnrollment(studentA)));

        when(examRoomAllocationRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        when(examSeatPlanRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        when(examInvigilatorAssignmentRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        var invigilatorUser1 = new User();
        invigilatorUser1.setId(invigilatorA);
        var invigilatorUser2 = new User();
        invigilatorUser2.setId(invigilatorB);
        when(userRepository.findAllById(List.of(invigilatorA, invigilatorB))).thenReturn(List.of(invigilatorUser2, invigilatorUser1));

        var result = examPlanningService.orchestrate(
                examId,
                List.of(
                        new ExamPlanningService.RoomPlanInput("b-101", 1, 1, 1),
                        new ExamPlanningService.RoomPlanInput("a-101", 1, 1, 1)
                ),
                List.of(invigilatorB, invigilatorA)
        );

        assertThat(result.plannedStudentCount()).isEqualTo(2);
        assertThat(result.roomCount()).isEqualTo(2);

        ArgumentCaptor<List<ExamSeatPlan>> seatCaptor = ArgumentCaptor.forClass(List.class);
        verify(examSeatPlanRepository).saveAll(seatCaptor.capture());
        var seatPlans = seatCaptor.getValue();
        assertThat(seatPlans).hasSize(2);
        assertThat(seatPlans.get(0).getSeatNumber()).isEqualTo(1);
        assertThat(seatPlans.get(0).getSeatLabel()).isEqualTo("A-101-R1C1");
        assertThat(seatPlans.get(0).getStudentUser().getId()).isEqualTo(studentA);
        assertThat(seatPlans.get(1).getSeatNumber()).isEqualTo(2);
        assertThat(seatPlans.get(1).getSeatLabel()).isEqualTo("B-101-R1C1");
        assertThat(seatPlans.get(1).getStudentUser().getId()).isEqualTo(studentB);

        ArgumentCaptor<List<ExamInvigilatorAssignment>> invigilatorCaptor = ArgumentCaptor.forClass(List.class);
        verify(examInvigilatorAssignmentRepository).saveAll(invigilatorCaptor.capture());
        var assignments = invigilatorCaptor.getValue();
        assertThat(assignments).hasSize(2);
        assertThat(assignments.get(0).getExamRoomAllocation().getRoomCode()).isEqualTo("A-101");
        assertThat(assignments.get(0).getInvigilatorUser().getId()).isEqualTo(invigilatorA);
        assertThat(assignments.get(1).getExamRoomAllocation().getRoomCode()).isEqualTo("B-101");
        assertThat(assignments.get(1).getInvigilatorUser().getId()).isEqualTo(invigilatorB);
    }

    @Test
    void orchestrateShouldFailWhenCapacityIsInsufficient() {
        UUID examId = UUID.randomUUID();
        UUID offeringId = UUID.randomUUID();
        var offering = new CourseOffering();
        offering.setId(offeringId);

        var exam = new Exam();
        exam.setId(examId);
        exam.setVersion(1);
        exam.setCourseOffering(offering);

        when(examRepository.findById(examId)).thenReturn(Optional.of(exam));
        when(enrollmentRecordRepository.findByCourseOfferingIdAndStatusOrderByStudentUserIdAsc(offeringId, EnrollmentStatus.ACTIVE))
                .thenReturn(List.of(buildEnrollment(UUID.randomUUID()), buildEnrollment(UUID.randomUUID())));

        assertThatThrownBy(() -> examPlanningService.orchestrate(
                examId,
                List.of(new ExamPlanningService.RoomPlanInput("A-101", 1, 1, 1)),
                List.of()
        )).isInstanceOf(ConflictException.class)
                .hasMessageContaining("capacity is less");
    }

    private EnrollmentRecord buildEnrollment(UUID studentId) {
        var student = new User();
        student.setId(studentId);

        var record = new EnrollmentRecord();
        record.setStudentUser(student);
        record.setStatus(EnrollmentStatus.ACTIVE);
        return record;
    }
}
