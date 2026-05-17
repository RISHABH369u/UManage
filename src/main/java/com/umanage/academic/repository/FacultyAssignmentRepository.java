package com.umanage.academic.repository;

import com.umanage.academic.entity.FacultyAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FacultyAssignmentRepository extends JpaRepository<FacultyAssignment, UUID> {
    boolean existsByCourseOfferingId(UUID courseOfferingId);
    boolean existsByCourseOfferingIdAndIdNot(UUID courseOfferingId, UUID id);
}
