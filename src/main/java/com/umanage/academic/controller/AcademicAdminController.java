package com.umanage.academic.controller;

import com.umanage.academic.dto.CourseOfferingRequest;
import com.umanage.academic.dto.CourseOfferingResponse;
import com.umanage.academic.dto.CourseRequest;
import com.umanage.academic.dto.CourseResponse;
import com.umanage.academic.dto.DepartmentRequest;
import com.umanage.academic.dto.DepartmentResponse;
import com.umanage.academic.dto.FacultyAssignmentRequest;
import com.umanage.academic.dto.FacultyAssignmentResponse;
import com.umanage.academic.dto.PrerequisiteRequest;
import com.umanage.academic.dto.PrerequisiteResponse;
import com.umanage.academic.dto.ProgramRequest;
import com.umanage.academic.dto.ProgramResponse;
import com.umanage.academic.dto.SemesterRequest;
import com.umanage.academic.dto.SemesterResponse;
import com.umanage.academic.mapper.AcademicMapper;
import com.umanage.academic.service.AcademicService;
import com.umanage.common.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/academic")
public class AcademicAdminController {

    private final AcademicService academicService;
    private final AcademicMapper academicMapper;

    public AcademicAdminController(AcademicService academicService, AcademicMapper academicMapper) {
        this.academicService = academicService;
        this.academicMapper = academicMapper;
    }

    @PostMapping("/departments")
    @PreAuthorize("hasAuthority('ACADEMIC_MANAGE')")
    public ResponseEntity<ApiResponse<DepartmentResponse>> createDepartment(@Valid @RequestBody DepartmentRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(academicMapper.toDepartmentResponse(academicService.createDepartment(request))));
    }

    @GetMapping("/departments")
    @PreAuthorize("hasAuthority('ACADEMIC_VIEW')")
    public ResponseEntity<ApiResponse<List<DepartmentResponse>>> listDepartments() {
        return ResponseEntity.ok(ApiResponse.ok(academicService.listDepartments().stream().map(academicMapper::toDepartmentResponse).toList()));
    }

    @GetMapping("/departments/{id}")
    @PreAuthorize("hasAuthority('ACADEMIC_VIEW')")
    public ResponseEntity<ApiResponse<DepartmentResponse>> getDepartment(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(academicMapper.toDepartmentResponse(academicService.getDepartment(id))));
    }

    @PutMapping("/departments/{id}")
    @PreAuthorize("hasAuthority('ACADEMIC_MANAGE')")
    public ResponseEntity<ApiResponse<DepartmentResponse>> updateDepartment(@PathVariable UUID id,
                                                                            @Valid @RequestBody DepartmentRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(academicMapper.toDepartmentResponse(academicService.updateDepartment(id, request))));
    }

    @DeleteMapping("/departments/{id}")
    @PreAuthorize("hasAuthority('ACADEMIC_MANAGE')")
    public ResponseEntity<ApiResponse<Void>> deleteDepartment(@PathVariable UUID id) {
        academicService.deleteDepartment(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping("/programs")
    @PreAuthorize("hasAuthority('ACADEMIC_MANAGE')")
    public ResponseEntity<ApiResponse<ProgramResponse>> createProgram(@Valid @RequestBody ProgramRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(academicMapper.toProgramResponse(academicService.createProgram(request))));
    }

    @GetMapping("/programs")
    @PreAuthorize("hasAuthority('ACADEMIC_VIEW')")
    public ResponseEntity<ApiResponse<List<ProgramResponse>>> listPrograms() {
        return ResponseEntity.ok(ApiResponse.ok(academicService.listPrograms().stream().map(academicMapper::toProgramResponse).toList()));
    }

    @GetMapping("/programs/{id}")
    @PreAuthorize("hasAuthority('ACADEMIC_VIEW')")
    public ResponseEntity<ApiResponse<ProgramResponse>> getProgram(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(academicMapper.toProgramResponse(academicService.getProgram(id))));
    }

    @PutMapping("/programs/{id}")
    @PreAuthorize("hasAuthority('ACADEMIC_MANAGE')")
    public ResponseEntity<ApiResponse<ProgramResponse>> updateProgram(@PathVariable UUID id,
                                                                      @Valid @RequestBody ProgramRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(academicMapper.toProgramResponse(academicService.updateProgram(id, request))));
    }

    @DeleteMapping("/programs/{id}")
    @PreAuthorize("hasAuthority('ACADEMIC_MANAGE')")
    public ResponseEntity<ApiResponse<Void>> deleteProgram(@PathVariable UUID id) {
        academicService.deleteProgram(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping("/semesters")
    @PreAuthorize("hasAuthority('ACADEMIC_MANAGE')")
    public ResponseEntity<ApiResponse<SemesterResponse>> createSemester(@Valid @RequestBody SemesterRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(academicMapper.toSemesterResponse(academicService.createSemester(request))));
    }

    @GetMapping("/semesters")
    @PreAuthorize("hasAuthority('ACADEMIC_VIEW')")
    public ResponseEntity<ApiResponse<List<SemesterResponse>>> listSemesters() {
        return ResponseEntity.ok(ApiResponse.ok(academicService.listSemesters().stream().map(academicMapper::toSemesterResponse).toList()));
    }

    @GetMapping("/semesters/{id}")
    @PreAuthorize("hasAuthority('ACADEMIC_VIEW')")
    public ResponseEntity<ApiResponse<SemesterResponse>> getSemester(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(academicMapper.toSemesterResponse(academicService.getSemester(id))));
    }

    @PutMapping("/semesters/{id}")
    @PreAuthorize("hasAuthority('ACADEMIC_MANAGE')")
    public ResponseEntity<ApiResponse<SemesterResponse>> updateSemester(@PathVariable UUID id,
                                                                        @Valid @RequestBody SemesterRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(academicMapper.toSemesterResponse(academicService.updateSemester(id, request))));
    }

    @DeleteMapping("/semesters/{id}")
    @PreAuthorize("hasAuthority('ACADEMIC_MANAGE')")
    public ResponseEntity<ApiResponse<Void>> deleteSemester(@PathVariable UUID id) {
        academicService.deleteSemester(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping("/courses")
    @PreAuthorize("hasAuthority('ACADEMIC_MANAGE')")
    public ResponseEntity<ApiResponse<CourseResponse>> createCourse(@Valid @RequestBody CourseRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(academicMapper.toCourseResponse(academicService.createCourse(request))));
    }

    @GetMapping("/courses")
    @PreAuthorize("hasAuthority('ACADEMIC_VIEW')")
    public ResponseEntity<ApiResponse<List<CourseResponse>>> listCourses() {
        return ResponseEntity.ok(ApiResponse.ok(academicService.listCourses().stream().map(academicMapper::toCourseResponse).toList()));
    }

    @GetMapping("/courses/{id}")
    @PreAuthorize("hasAuthority('ACADEMIC_VIEW')")
    public ResponseEntity<ApiResponse<CourseResponse>> getCourse(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(academicMapper.toCourseResponse(academicService.getCourse(id))));
    }

    @PutMapping("/courses/{id}")
    @PreAuthorize("hasAuthority('ACADEMIC_MANAGE')")
    public ResponseEntity<ApiResponse<CourseResponse>> updateCourse(@PathVariable UUID id,
                                                                    @Valid @RequestBody CourseRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(academicMapper.toCourseResponse(academicService.updateCourse(id, request))));
    }

    @DeleteMapping("/courses/{id}")
    @PreAuthorize("hasAuthority('ACADEMIC_MANAGE')")
    public ResponseEntity<ApiResponse<Void>> deleteCourse(@PathVariable UUID id) {
        academicService.deleteCourse(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping("/offerings")
    @PreAuthorize("hasAuthority('ACADEMIC_MANAGE')")
    public ResponseEntity<ApiResponse<CourseOfferingResponse>> createCourseOffering(@Valid @RequestBody CourseOfferingRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(academicMapper.toCourseOfferingResponse(academicService.createCourseOffering(request))));
    }

    @GetMapping("/offerings")
    @PreAuthorize("hasAuthority('ACADEMIC_VIEW')")
    public ResponseEntity<ApiResponse<List<CourseOfferingResponse>>> listCourseOfferings() {
        return ResponseEntity.ok(ApiResponse.ok(academicService.listCourseOfferings().stream().map(academicMapper::toCourseOfferingResponse).toList()));
    }

    @GetMapping("/offerings/{id}")
    @PreAuthorize("hasAuthority('ACADEMIC_VIEW')")
    public ResponseEntity<ApiResponse<CourseOfferingResponse>> getCourseOffering(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(academicMapper.toCourseOfferingResponse(academicService.getCourseOffering(id))));
    }

    @PutMapping("/offerings/{id}")
    @PreAuthorize("hasAuthority('ACADEMIC_MANAGE')")
    public ResponseEntity<ApiResponse<CourseOfferingResponse>> updateCourseOffering(@PathVariable UUID id,
                                                                                     @Valid @RequestBody CourseOfferingRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(academicMapper.toCourseOfferingResponse(academicService.updateCourseOffering(id, request))));
    }

    @DeleteMapping("/offerings/{id}")
    @PreAuthorize("hasAuthority('ACADEMIC_MANAGE')")
    public ResponseEntity<ApiResponse<Void>> deleteCourseOffering(@PathVariable UUID id) {
        academicService.deleteCourseOffering(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping("/prerequisites")
    @PreAuthorize("hasAuthority('ACADEMIC_MANAGE')")
    public ResponseEntity<ApiResponse<PrerequisiteResponse>> createPrerequisite(@Valid @RequestBody PrerequisiteRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(academicMapper.toPrerequisiteResponse(academicService.createPrerequisite(request))));
    }

    @GetMapping("/prerequisites")
    @PreAuthorize("hasAuthority('ACADEMIC_VIEW')")
    public ResponseEntity<ApiResponse<List<PrerequisiteResponse>>> listPrerequisites() {
        return ResponseEntity.ok(ApiResponse.ok(academicService.listPrerequisites().stream().map(academicMapper::toPrerequisiteResponse).toList()));
    }

    @GetMapping("/prerequisites/{id}")
    @PreAuthorize("hasAuthority('ACADEMIC_VIEW')")
    public ResponseEntity<ApiResponse<PrerequisiteResponse>> getPrerequisite(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(academicMapper.toPrerequisiteResponse(academicService.getPrerequisite(id))));
    }

    @PutMapping("/prerequisites/{id}")
    @PreAuthorize("hasAuthority('ACADEMIC_MANAGE')")
    public ResponseEntity<ApiResponse<PrerequisiteResponse>> updatePrerequisite(@PathVariable UUID id,
                                                                                 @Valid @RequestBody PrerequisiteRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(academicMapper.toPrerequisiteResponse(academicService.updatePrerequisite(id, request))));
    }

    @DeleteMapping("/prerequisites/{id}")
    @PreAuthorize("hasAuthority('ACADEMIC_MANAGE')")
    public ResponseEntity<ApiResponse<Void>> deletePrerequisite(@PathVariable UUID id) {
        academicService.deletePrerequisite(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PostMapping("/faculty-assignments")
    @PreAuthorize("hasAuthority('ACADEMIC_MANAGE')")
    public ResponseEntity<ApiResponse<FacultyAssignmentResponse>> createFacultyAssignment(@Valid @RequestBody FacultyAssignmentRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(academicMapper.toFacultyAssignmentResponse(academicService.createFacultyAssignment(request))));
    }

    @GetMapping("/faculty-assignments")
    @PreAuthorize("hasAuthority('ACADEMIC_VIEW')")
    public ResponseEntity<ApiResponse<List<FacultyAssignmentResponse>>> listFacultyAssignments() {
        return ResponseEntity.ok(ApiResponse.ok(academicService.listFacultyAssignments().stream().map(academicMapper::toFacultyAssignmentResponse).toList()));
    }

    @GetMapping("/faculty-assignments/{id}")
    @PreAuthorize("hasAuthority('ACADEMIC_VIEW')")
    public ResponseEntity<ApiResponse<FacultyAssignmentResponse>> getFacultyAssignment(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(academicMapper.toFacultyAssignmentResponse(academicService.getFacultyAssignment(id))));
    }

    @PutMapping("/faculty-assignments/{id}")
    @PreAuthorize("hasAuthority('ACADEMIC_MANAGE')")
    public ResponseEntity<ApiResponse<FacultyAssignmentResponse>> updateFacultyAssignment(@PathVariable UUID id,
                                                                                           @Valid @RequestBody FacultyAssignmentRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(academicMapper.toFacultyAssignmentResponse(academicService.updateFacultyAssignment(id, request))));
    }

    @DeleteMapping("/faculty-assignments/{id}")
    @PreAuthorize("hasAuthority('ACADEMIC_MANAGE')")
    public ResponseEntity<ApiResponse<Void>> deleteFacultyAssignment(@PathVariable UUID id) {
        academicService.deleteFacultyAssignment(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
