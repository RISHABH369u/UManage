package com.umanage.academic.mapper;

import com.umanage.academic.dto.CourseOfferingResponse;
import com.umanage.academic.dto.CourseResponse;
import com.umanage.academic.dto.DepartmentResponse;
import com.umanage.academic.dto.FacultyAssignmentResponse;
import com.umanage.academic.dto.PrerequisiteResponse;
import com.umanage.academic.dto.ProgramResponse;
import com.umanage.academic.dto.SemesterResponse;
import com.umanage.academic.entity.Course;
import com.umanage.academic.entity.CourseOffering;
import com.umanage.academic.entity.Department;
import com.umanage.academic.entity.FacultyAssignment;
import com.umanage.academic.entity.Prerequisite;
import com.umanage.academic.entity.Program;
import com.umanage.academic.entity.Semester;
import org.springframework.stereotype.Component;

@Component
public class AcademicMapper {

    public DepartmentResponse toDepartmentResponse(Department department) {
        return new DepartmentResponse(department.getId(), department.getCode(), department.getName());
    }

    public ProgramResponse toProgramResponse(Program program) {
        return new ProgramResponse(
                program.getId(),
                program.getCode(),
                program.getName(),
                program.getDepartment().getId(),
                program.getDepartment().getCode(),
                program.getDepartment().getName()
        );
    }

    public SemesterResponse toSemesterResponse(Semester semester) {
        return new SemesterResponse(
                semester.getId(),
                semester.getName(),
                semester.getSequenceNumber(),
                semester.getProgram().getId(),
                semester.getProgram().getCode(),
                semester.getProgram().getName()
        );
    }

    public CourseResponse toCourseResponse(Course course) {
        return new CourseResponse(
                course.getId(),
                course.getCode(),
                course.getTitle(),
                course.getCredits(),
                course.getDepartment().getId(),
                course.getDepartment().getCode(),
                course.getDepartment().getName()
        );
    }

    public CourseOfferingResponse toCourseOfferingResponse(CourseOffering offering) {
        return new CourseOfferingResponse(
                offering.getId(),
                offering.getCourse().getId(),
                offering.getCourse().getCode(),
                offering.getCourse().getTitle(),
                offering.getSemester().getId(),
                offering.getSemester().getName(),
                offering.getSemester().getSequenceNumber(),
                offering.getSectionCode(),
                offering.getCapacity()
        );
    }

    public PrerequisiteResponse toPrerequisiteResponse(Prerequisite prerequisite) {
        return new PrerequisiteResponse(
                prerequisite.getId(),
                prerequisite.getCourse().getId(),
                prerequisite.getCourse().getCode(),
                prerequisite.getCourse().getTitle(),
                prerequisite.getPrerequisiteCourse().getId(),
                prerequisite.getPrerequisiteCourse().getCode(),
                prerequisite.getPrerequisiteCourse().getTitle()
        );
    }

    public FacultyAssignmentResponse toFacultyAssignmentResponse(FacultyAssignment assignment) {
        return new FacultyAssignmentResponse(
                assignment.getId(),
                assignment.getCourseOffering().getId(),
                assignment.getCourseOffering().getCourse().getCode(),
                assignment.getCourseOffering().getSectionCode(),
                assignment.getFaculty().getId(),
                assignment.getFaculty().getEmail(),
                assignment.getFaculty().getFirstName() + " " + assignment.getFaculty().getLastName()
        );
    }
}
