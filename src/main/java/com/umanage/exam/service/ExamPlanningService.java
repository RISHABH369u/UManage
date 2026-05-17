package com.umanage.exam.service;

import com.umanage.academic.repository.CourseOfferingRepository;
import com.umanage.common.exception.ConflictException;
import com.umanage.common.exception.ResourceNotFoundException;
import com.umanage.enrollment.entity.EnrollmentStatus;
import com.umanage.enrollment.repository.EnrollmentRecordRepository;
import com.umanage.exam.entity.Exam;
import com.umanage.exam.entity.ExamInvigilatorAssignment;
import com.umanage.exam.entity.ExamRoomAllocation;
import com.umanage.exam.entity.ExamSeatPlan;
import com.umanage.exam.entity.ExamStatus;
import com.umanage.exam.repository.ExamInvigilatorAssignmentRepository;
import com.umanage.exam.repository.ExamRepository;
import com.umanage.exam.repository.ExamRoomAllocationRepository;
import com.umanage.exam.repository.ExamSeatPlanRepository;
import com.umanage.users.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

@Service
public class ExamPlanningService {

    private final ExamRepository examRepository;
    private final CourseOfferingRepository courseOfferingRepository;
    private final EnrollmentRecordRepository enrollmentRecordRepository;
    private final UserRepository userRepository;
    private final ExamRoomAllocationRepository examRoomAllocationRepository;
    private final ExamInvigilatorAssignmentRepository examInvigilatorAssignmentRepository;
    private final ExamSeatPlanRepository examSeatPlanRepository;

    public ExamPlanningService(ExamRepository examRepository,
                               CourseOfferingRepository courseOfferingRepository,
                               EnrollmentRecordRepository enrollmentRecordRepository,
                               UserRepository userRepository,
                               ExamRoomAllocationRepository examRoomAllocationRepository,
                               ExamInvigilatorAssignmentRepository examInvigilatorAssignmentRepository,
                               ExamSeatPlanRepository examSeatPlanRepository) {
        this.examRepository = examRepository;
        this.courseOfferingRepository = courseOfferingRepository;
        this.enrollmentRecordRepository = enrollmentRecordRepository;
        this.userRepository = userRepository;
        this.examRoomAllocationRepository = examRoomAllocationRepository;
        this.examInvigilatorAssignmentRepository = examInvigilatorAssignmentRepository;
        this.examSeatPlanRepository = examSeatPlanRepository;
    }

    @Transactional
    public Exam scheduleExam(UUID courseOfferingId,
                             String title,
                             OffsetDateTime scheduledAt,
                             int durationMinutes,
                             int version) {
        if (title == null || title.isBlank()) {
            throw new ConflictException("Exam title is required");
        }
        if (scheduledAt == null) {
            throw new ConflictException("Exam schedule datetime is required");
        }
        if (durationMinutes <= 0) {
            throw new ConflictException("Exam duration must be positive");
        }
        if (version <= 0) {
            throw new ConflictException("Exam version must be positive");
        }

        var offering = courseOfferingRepository.findById(courseOfferingId)
                .orElseThrow(() -> new ResourceNotFoundException("Course offering not found"));

        var exam = new Exam();
        exam.setCourseOffering(offering);
        exam.setTitle(title.trim());
        exam.setScheduledAt(scheduledAt);
        exam.setDurationMinutes(durationMinutes);
        exam.setVersion(version);
        exam.setStatus(ExamStatus.DRAFT);
        return examRepository.save(exam);
    }

    @Transactional
    public OrchestrationResult orchestrate(UUID examId,
                                           List<RoomPlanInput> roomInputs,
                                           List<UUID> invigilatorUserIds) {
        var exam = examRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam not found"));

        var normalizedRooms = normalizeRooms(roomInputs);
        if (normalizedRooms.isEmpty()) {
            throw new ConflictException("At least one room allocation is required");
        }

        var activeEnrollments = enrollmentRecordRepository.findByCourseOfferingIdAndStatusOrderByStudentUserIdAsc(
                exam.getCourseOffering().getId(),
                EnrollmentStatus.ACTIVE
        );

        var totalCapacity = normalizedRooms.stream().mapToInt(RoomPlanInput::capacity).sum();
        if (totalCapacity < activeEnrollments.size()) {
            throw new ConflictException("Room capacity is less than total active enrollments");
        }

        examSeatPlanRepository.deleteByExamId(examId);
        examInvigilatorAssignmentRepository.deleteByExamId(examId);
        examRoomAllocationRepository.deleteByExamId(examId);

        var roomAllocations = createRoomAllocations(exam, normalizedRooms);
        createInvigilatorAssignments(exam, roomAllocations, invigilatorUserIds);
        var seatPlans = createSeatPlans(exam, roomAllocations, activeEnrollments);

        exam.setStatus(ExamStatus.PLANNED);
        examRepository.save(exam);

        return new OrchestrationResult(exam.getId(), seatPlans.size(), roomAllocations.size());
    }

    private List<RoomPlanInput> normalizeRooms(List<RoomPlanInput> roomInputs) {
        if (roomInputs == null) {
            return List.of();
        }

        var deduplicated = new LinkedHashMap<String, RoomPlanInput>();
        for (var roomInput : roomInputs) {
            if (roomInput == null || roomInput.roomCode() == null || roomInput.roomCode().isBlank()) {
                throw new ConflictException("Room code is required for all room allocations");
            }
            var roomCode = roomInput.roomCode().trim().toUpperCase();
            var capacity = roomInput.capacity();
            var rowCount = roomInput.rowCount() <= 0 ? 1 : roomInput.rowCount();
            var seatsPerRow = roomInput.seatsPerRow() <= 0 ? 1 : roomInput.seatsPerRow();

            if (capacity <= 0) {
                throw new ConflictException("Room capacity must be positive");
            }
            if ((long) rowCount * seatsPerRow < capacity) {
                throw new ConflictException("Room geometry must accommodate its capacity");
            }

            deduplicated.put(roomCode, new RoomPlanInput(roomCode, capacity, rowCount, seatsPerRow));
        }

        return deduplicated.values().stream()
                .sorted(Comparator.comparing(RoomPlanInput::roomCode))
                .toList();
    }

    private List<ExamRoomAllocation> createRoomAllocations(Exam exam, List<RoomPlanInput> roomInputs) {
        var allocations = new ArrayList<ExamRoomAllocation>();
        int order = 1;
        for (var roomInput : roomInputs) {
            var allocation = new ExamRoomAllocation();
            allocation.setExam(exam);
            allocation.setRoomCode(roomInput.roomCode());
            allocation.setCapacity(roomInput.capacity());
            allocation.setRowCount(roomInput.rowCount());
            allocation.setSeatsPerRow(roomInput.seatsPerRow());
            allocation.setDisplayOrder(order++);
            allocations.add(allocation);
        }
        return examRoomAllocationRepository.saveAll(allocations);
    }

    private void createInvigilatorAssignments(Exam exam,
                                              List<ExamRoomAllocation> roomAllocations,
                                              List<UUID> invigilatorUserIds) {
        if (invigilatorUserIds == null || invigilatorUserIds.isEmpty()) {
            return;
        }

        var normalizedIds = invigilatorUserIds.stream().distinct().sorted().toList();
        if (normalizedIds.size() < roomAllocations.size()) {
            throw new ConflictException("At least one unique invigilator is required per room");
        }

        var invigilators = userRepository.findAllById(normalizedIds).stream()
                .sorted(Comparator.comparing(user -> user.getId().toString()))
                .toList();

        if (invigilators.size() != normalizedIds.size()) {
            throw new ResourceNotFoundException("One or more invigilator users were not found");
        }

        var assignments = new ArrayList<ExamInvigilatorAssignment>();
        for (int i = 0; i < roomAllocations.size(); i++) {
            var assignment = new ExamInvigilatorAssignment();
            assignment.setExam(exam);
            assignment.setExamRoomAllocation(roomAllocations.get(i));
            assignment.setInvigilatorUser(invigilators.get(i));
            assignment.setAssignmentOrder(i + 1);
            assignments.add(assignment);
        }
        examInvigilatorAssignmentRepository.saveAll(assignments);
    }

    private List<ExamSeatPlan> createSeatPlans(Exam exam,
                                               List<ExamRoomAllocation> roomAllocations,
                                               List<com.umanage.enrollment.entity.EnrollmentRecord> activeEnrollments) {
        var sortedEnrollments = activeEnrollments.stream()
                .sorted(Comparator.comparing(record -> record.getStudentUser().getId().toString()))
                .toList();

        var plans = new ArrayList<ExamSeatPlan>();
        int studentIndex = 0;
        int globalSeatNumber = 1;

        for (var room : roomAllocations) {
            for (int roomSeat = 1; roomSeat <= room.getCapacity() && studentIndex < sortedEnrollments.size(); roomSeat++) {
                var enrollment = sortedEnrollments.get(studentIndex++);
                var seat = new ExamSeatPlan();
                seat.setExam(exam);
                seat.setExamRoomAllocation(room);
                seat.setStudentUser(enrollment.getStudentUser());
                seat.setSeatNumber(globalSeatNumber++);
                seat.setSeatLabel(buildSeatLabel(room.getRoomCode(), roomSeat, room.getSeatsPerRow()));
                seat.setExamVersion(exam.getVersion());
                plans.add(seat);
            }
        }

        return examSeatPlanRepository.saveAll(plans);
    }

    private String buildSeatLabel(String roomCode, int localSeatNumber, int seatsPerRow) {
        var row = ((localSeatNumber - 1) / seatsPerRow) + 1;
        var column = ((localSeatNumber - 1) % seatsPerRow) + 1;
        return roomCode + "-R" + row + "C" + column;
    }

    public record RoomPlanInput(String roomCode, int capacity, int rowCount, int seatsPerRow) {
    }

    public record OrchestrationResult(UUID examId, int plannedStudentCount, int roomCount) {
    }
}
