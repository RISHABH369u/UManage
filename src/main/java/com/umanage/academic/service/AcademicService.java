package com.umanage.academic.service;

import com.umanage.academic.dto.CourseOfferingRequest;
import com.umanage.academic.dto.CourseRequest;
import com.umanage.academic.dto.DepartmentRequest;
import com.umanage.academic.dto.FacultyAssignmentRequest;
import com.umanage.academic.dto.PrerequisiteRequest;
import com.umanage.academic.dto.ProgramRequest;
import com.umanage.academic.dto.SemesterRequest;
import com.umanage.academic.entity.Course;
import com.umanage.academic.entity.CourseOffering;
import com.umanage.academic.entity.Department;
import com.umanage.academic.entity.FacultyAssignment;
import com.umanage.academic.entity.Prerequisite;
import com.umanage.academic.entity.Program;
import com.umanage.academic.entity.Semester;
import com.umanage.academic.repository.CourseOfferingRepository;
import com.umanage.academic.repository.CourseRepository;
import com.umanage.academic.repository.DepartmentRepository;
import com.umanage.academic.repository.FacultyAssignmentRepository;
import com.umanage.academic.repository.PrerequisiteRepository;
import com.umanage.academic.repository.ProgramRepository;
import com.umanage.academic.repository.SemesterRepository;
import com.umanage.common.exception.ConflictException;
import com.umanage.common.exception.ResourceNotFoundException;
import com.umanage.users.repository.UserRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Service
public class AcademicService {

    private final DepartmentRepository departmentRepository;
    private final ProgramRepository programRepository;
    private final SemesterRepository semesterRepository;
    private final CourseRepository courseRepository;
    private final CourseOfferingRepository courseOfferingRepository;
    private final PrerequisiteRepository prerequisiteRepository;
    private final FacultyAssignmentRepository facultyAssignmentRepository;
    private final UserRepository userRepository;

    public AcademicService(DepartmentRepository departmentRepository,
                           ProgramRepository programRepository,
                           SemesterRepository semesterRepository,
                           CourseRepository courseRepository,
                           CourseOfferingRepository courseOfferingRepository,
                           PrerequisiteRepository prerequisiteRepository,
                           FacultyAssignmentRepository facultyAssignmentRepository,
                           UserRepository userRepository) {
        this.departmentRepository = departmentRepository;
        this.programRepository = programRepository;
        this.semesterRepository = semesterRepository;
        this.courseRepository = courseRepository;
        this.courseOfferingRepository = courseOfferingRepository;
        this.prerequisiteRepository = prerequisiteRepository;
        this.facultyAssignmentRepository = facultyAssignmentRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Department createDepartment(DepartmentRequest request) {
        if (departmentRepository.existsByCodeIgnoreCase(request.code().trim())) {
            throw new ConflictException("Department code already exists");
        }
        var department = new Department();
        applyDepartment(department, request);
        return departmentRepository.save(department);
    }

    @Transactional(readOnly = true)
    public List<Department> listDepartments() {
        return departmentRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
    }

    @Transactional(readOnly = true)
    public Department getDepartment(UUID id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
    }

    @Transactional
    public Department updateDepartment(UUID id, DepartmentRequest request) {
        var department = getDepartment(id);
        var normalizedCode = request.code().trim();
        departmentRepository.findByCodeIgnoreCase(normalizedCode)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new ConflictException("Department code already exists");
                });
        applyDepartment(department, request);
        return departmentRepository.save(department);
    }

    @Transactional
    public void deleteDepartment(UUID id) {
        departmentRepository.delete(getDepartment(id));
    }

    @Transactional
    public Program createProgram(ProgramRequest request) {
        if (programRepository.existsByCodeIgnoreCase(request.code().trim())) {
            throw new ConflictException("Program code already exists");
        }
        var program = new Program();
        applyProgram(program, request);
        return programRepository.save(program);
    }

    @Transactional(readOnly = true)
    public List<Program> listPrograms() {
        return programRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
    }

    @Transactional(readOnly = true)
    public Program getProgram(UUID id) {
        return programRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Program not found"));
    }

    @Transactional
    public Program updateProgram(UUID id, ProgramRequest request) {
        var program = getProgram(id);
        var normalizedCode = request.code().trim();
        programRepository.findByCodeIgnoreCase(normalizedCode)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new ConflictException("Program code already exists");
                });
        applyProgram(program, request);
        return programRepository.save(program);
    }

    @Transactional
    public void deleteProgram(UUID id) {
        programRepository.delete(getProgram(id));
    }

    @Transactional
    public Semester createSemester(SemesterRequest request) {
        if (semesterRepository.existsByProgramIdAndSequenceNumber(request.programId(), request.sequenceNumber())) {
            throw new ConflictException("Semester sequence already exists for this program");
        }
        var semester = new Semester();
        applySemester(semester, request);
        return semesterRepository.save(semester);
    }

    @Transactional(readOnly = true)
    public List<Semester> listSemesters() {
        return semesterRepository.findAll(Sort.by(Sort.Direction.ASC, "sequenceNumber"));
    }

    @Transactional(readOnly = true)
    public Semester getSemester(UUID id) {
        return semesterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Semester not found"));
    }

    @Transactional
    public Semester updateSemester(UUID id, SemesterRequest request) {
        var semester = getSemester(id);
        if (semesterRepository.existsByProgramIdAndSequenceNumberAndIdNot(request.programId(), request.sequenceNumber(), id)) {
            throw new ConflictException("Semester sequence already exists for this program");
        }
        applySemester(semester, request);
        return semesterRepository.save(semester);
    }

    @Transactional
    public void deleteSemester(UUID id) {
        semesterRepository.delete(getSemester(id));
    }

    @Transactional
    public Course createCourse(CourseRequest request) {
        if (courseRepository.existsByCodeIgnoreCase(request.code().trim())) {
            throw new ConflictException("Course code already exists");
        }
        var course = new Course();
        applyCourse(course, request);
        return courseRepository.save(course);
    }

    @Transactional(readOnly = true)
    public List<Course> listCourses() {
        return courseRepository.findAll(Sort.by(Sort.Direction.ASC, "code"));
    }

    @Transactional(readOnly = true)
    public Course getCourse(UUID id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
    }

    @Transactional
    public Course updateCourse(UUID id, CourseRequest request) {
        var course = getCourse(id);
        var normalizedCode = request.code().trim();
        courseRepository.findByCodeIgnoreCase(normalizedCode)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new ConflictException("Course code already exists");
                });
        applyCourse(course, request);
        return courseRepository.save(course);
    }

    @Transactional
    public void deleteCourse(UUID id) {
        courseRepository.delete(getCourse(id));
    }

    @Transactional
    public CourseOffering createCourseOffering(CourseOfferingRequest request) {
        if (courseOfferingRepository.existsByCourseIdAndSemesterIdAndSectionCodeIgnoreCase(
                request.courseId(), request.semesterId(), request.sectionCode().trim())) {
            throw new ConflictException("Course offering section already exists for this course and semester");
        }
        var offering = new CourseOffering();
        applyCourseOffering(offering, request);
        return courseOfferingRepository.save(offering);
    }

    @Transactional(readOnly = true)
    public List<CourseOffering> listCourseOfferings() {
        return courseOfferingRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    @Transactional(readOnly = true)
    public CourseOffering getCourseOffering(UUID id) {
        return courseOfferingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course offering not found"));
    }

    @Transactional
    public CourseOffering updateCourseOffering(UUID id, CourseOfferingRequest request) {
        var offering = getCourseOffering(id);
        if (courseOfferingRepository.existsByCourseIdAndSemesterIdAndSectionCodeIgnoreCaseAndIdNot(
                request.courseId(), request.semesterId(), request.sectionCode().trim(), id)) {
            throw new ConflictException("Course offering section already exists for this course and semester");
        }
        applyCourseOffering(offering, request);
        return courseOfferingRepository.save(offering);
    }

    @Transactional
    public void deleteCourseOffering(UUID id) {
        courseOfferingRepository.delete(getCourseOffering(id));
    }

    @Transactional
    public Prerequisite createPrerequisite(PrerequisiteRequest request) {
        validatePrerequisiteCourses(request.courseId(), request.prerequisiteCourseId());
        if (prerequisiteRepository.existsByCourseIdAndPrerequisiteCourseId(request.courseId(), request.prerequisiteCourseId())) {
            throw new ConflictException("Prerequisite already exists");
        }
        var prerequisite = new Prerequisite();
        applyPrerequisite(prerequisite, request);
        return prerequisiteRepository.save(prerequisite);
    }

    @Transactional(readOnly = true)
    public List<Prerequisite> listPrerequisites() {
        return prerequisiteRepository.findAll(Sort.by(Sort.Direction.ASC, "createdAt"));
    }

    @Transactional(readOnly = true)
    public Prerequisite getPrerequisite(UUID id) {
        return prerequisiteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prerequisite not found"));
    }

    @Transactional
    public Prerequisite updatePrerequisite(UUID id, PrerequisiteRequest request) {
        var prerequisite = getPrerequisite(id);
        validatePrerequisiteCourses(request.courseId(), request.prerequisiteCourseId());
        if (prerequisiteRepository.existsByCourseIdAndPrerequisiteCourseIdAndIdNot(request.courseId(), request.prerequisiteCourseId(), id)) {
            throw new ConflictException("Prerequisite already exists");
        }
        applyPrerequisite(prerequisite, request);
        return prerequisiteRepository.save(prerequisite);
    }

    @Transactional
    public void deletePrerequisite(UUID id) {
        prerequisiteRepository.delete(getPrerequisite(id));
    }

    @Transactional
    public FacultyAssignment createFacultyAssignment(FacultyAssignmentRequest request) {
        if (facultyAssignmentRepository.existsByCourseOfferingId(request.courseOfferingId())) {
            throw new ConflictException("Faculty assignment already exists for this course offering");
        }
        var assignment = new FacultyAssignment();
        applyFacultyAssignment(assignment, request);
        return facultyAssignmentRepository.save(assignment);
    }

    @Transactional(readOnly = true)
    public List<FacultyAssignment> listFacultyAssignments() {
        return facultyAssignmentRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    @Transactional(readOnly = true)
    public FacultyAssignment getFacultyAssignment(UUID id) {
        return facultyAssignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty assignment not found"));
    }

    @Transactional
    public FacultyAssignment updateFacultyAssignment(UUID id, FacultyAssignmentRequest request) {
        var assignment = getFacultyAssignment(id);
        if (facultyAssignmentRepository.existsByCourseOfferingIdAndIdNot(request.courseOfferingId(), id)) {
            throw new ConflictException("Faculty assignment already exists for this course offering");
        }
        applyFacultyAssignment(assignment, request);
        return facultyAssignmentRepository.save(assignment);
    }

    @Transactional
    public void deleteFacultyAssignment(UUID id) {
        facultyAssignmentRepository.delete(getFacultyAssignment(id));
    }

    private void applyDepartment(Department department, DepartmentRequest request) {
        department.setCode(request.code().trim().toUpperCase());
        department.setName(request.name().trim());
    }

    private void applyProgram(Program program, ProgramRequest request) {
        program.setCode(request.code().trim().toUpperCase());
        program.setName(request.name().trim());
        program.setDepartment(getDepartment(request.departmentId()));
    }

    private void applySemester(Semester semester, SemesterRequest request) {
        semester.setName(request.name().trim());
        semester.setSequenceNumber(request.sequenceNumber());
        semester.setProgram(getProgram(request.programId()));
    }

    private void applyCourse(Course course, CourseRequest request) {
        course.setCode(request.code().trim().toUpperCase());
        course.setTitle(request.title().trim());
        course.setCredits(request.credits().setScale(2, RoundingMode.HALF_UP));
        course.setDepartment(getDepartment(request.departmentId()));
    }

    private void applyCourseOffering(CourseOffering offering, CourseOfferingRequest request) {
        offering.setCourse(getCourse(request.courseId()));
        offering.setSemester(getSemester(request.semesterId()));
        offering.setSectionCode(request.sectionCode().trim().toUpperCase());
        offering.setCapacity(request.capacity());
    }

    private void applyPrerequisite(Prerequisite prerequisite, PrerequisiteRequest request) {
        prerequisite.setCourse(getCourse(request.courseId()));
        prerequisite.setPrerequisiteCourse(getCourse(request.prerequisiteCourseId()));
    }

    private void applyFacultyAssignment(FacultyAssignment assignment, FacultyAssignmentRequest request) {
        assignment.setCourseOffering(getCourseOffering(request.courseOfferingId()));
        assignment.setFaculty(userRepository.findById(request.facultyUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Faculty user not found")));
    }

    private void validatePrerequisiteCourses(UUID courseId, UUID prerequisiteCourseId) {
        if (courseId.equals(prerequisiteCourseId)) {
            throw new ConflictException("A course cannot be its own prerequisite");
        }
    }
}
