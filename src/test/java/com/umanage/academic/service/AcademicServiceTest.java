package com.umanage.academic.service;

import com.umanage.academic.dto.DepartmentRequest;
import com.umanage.academic.dto.PrerequisiteRequest;
import com.umanage.academic.dto.SemesterRequest;
import com.umanage.academic.entity.Course;
import com.umanage.academic.entity.Department;
import com.umanage.academic.entity.Prerequisite;
import com.umanage.academic.entity.Program;
import com.umanage.academic.repository.CourseOfferingRepository;
import com.umanage.academic.repository.CourseRepository;
import com.umanage.academic.repository.DepartmentRepository;
import com.umanage.academic.repository.FacultyAssignmentRepository;
import com.umanage.academic.repository.PrerequisiteRepository;
import com.umanage.academic.repository.ProgramRepository;
import com.umanage.academic.repository.SemesterRepository;
import com.umanage.common.exception.ConflictException;
import com.umanage.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AcademicServiceTest {

    @Mock
    private DepartmentRepository departmentRepository;
    @Mock
    private ProgramRepository programRepository;
    @Mock
    private SemesterRepository semesterRepository;
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private CourseOfferingRepository courseOfferingRepository;
    @Mock
    private PrerequisiteRepository prerequisiteRepository;
    @Mock
    private FacultyAssignmentRepository facultyAssignmentRepository;
    @Mock
    private UserRepository userRepository;

    private AcademicService academicService;

    @BeforeEach
    void setUp() {
        academicService = new AcademicService(
                departmentRepository,
                programRepository,
                semesterRepository,
                courseRepository,
                courseOfferingRepository,
                prerequisiteRepository,
                facultyAssignmentRepository,
                userRepository
        );
    }

    @Test
    void createDepartmentShouldNormalizeCode() {
        when(departmentRepository.existsByCodeIgnoreCase("cse")).thenReturn(false);
        when(departmentRepository.save(any(Department.class))).thenAnswer(invocation -> invocation.getArgument(0, Department.class));

        academicService.createDepartment(new DepartmentRequest(" cse ", "Computer Science"));

        var captor = ArgumentCaptor.forClass(Department.class);
        verify(departmentRepository).save(captor.capture());
        assertThat(captor.getValue().getCode()).isEqualTo("CSE");
    }

    @Test
    void createSemesterShouldFailOnDuplicateSequenceInProgram() {
        UUID programId = UUID.randomUUID();
        when(semesterRepository.existsByProgramIdAndSequenceNumber(programId, 1)).thenReturn(true);

        assertThatThrownBy(() -> academicService.createSemester(new SemesterRequest("Semester 1", 1, programId)))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Semester sequence already exists");
    }

    @Test
    void createPrerequisiteShouldFailOnSelfReference() {
        UUID sameCourseId = UUID.randomUUID();

        assertThatThrownBy(() -> academicService.createPrerequisite(new PrerequisiteRequest(sameCourseId, sameCourseId)))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("cannot be its own prerequisite");
    }

    @Test
    void createPrerequisiteShouldPersistBothCourses() {
        UUID courseId = UUID.randomUUID();
        UUID prerequisiteCourseId = UUID.randomUUID();

        when(prerequisiteRepository.existsByCourseIdAndPrerequisiteCourseId(courseId, prerequisiteCourseId)).thenReturn(false);

        var mainCourse = new Course();
        mainCourse.setId(courseId);
        mainCourse.setCode("CS201");
        var prerequisiteCourse = new Course();
        prerequisiteCourse.setId(prerequisiteCourseId);
        prerequisiteCourse.setCode("CS101");

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(mainCourse));
        when(courseRepository.findById(prerequisiteCourseId)).thenReturn(Optional.of(prerequisiteCourse));
        when(prerequisiteRepository.save(any(Prerequisite.class))).thenAnswer(invocation -> invocation.getArgument(0, Prerequisite.class));

        var saved = academicService.createPrerequisite(new PrerequisiteRequest(courseId, prerequisiteCourseId));
        assertThat(saved.getCourse().getId()).isEqualTo(courseId);
        assertThat(saved.getPrerequisiteCourse().getId()).isEqualTo(prerequisiteCourseId);
    }

    @Test
    void updateSemesterShouldAllowSameProgramSequenceOnCurrentEntity() {
        UUID semesterId = UUID.randomUUID();
        UUID programId = UUID.randomUUID();

        var department = new Department();
        department.setId(UUID.randomUUID());
        var program = new Program();
        program.setId(programId);
        program.setDepartment(department);
        var semester = new com.umanage.academic.entity.Semester();
        semester.setId(semesterId);
        semester.setProgram(program);

        when(semesterRepository.findById(semesterId)).thenReturn(Optional.of(semester));
        when(semesterRepository.existsByProgramIdAndSequenceNumberAndIdNot(programId, 1, semesterId)).thenReturn(false);
        when(programRepository.findById(programId)).thenReturn(Optional.of(program));
        when(semesterRepository.save(any(com.umanage.academic.entity.Semester.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var updated = academicService.updateSemester(semesterId, new SemesterRequest("Sem 1", 1, programId));
        assertThat(updated.getSequenceNumber()).isEqualTo(1);
    }
}
