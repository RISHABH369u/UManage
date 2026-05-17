package com.umanage.academic.repository;

import com.umanage.academic.entity.CourseOffering;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CourseOfferingRepository extends JpaRepository<CourseOffering, UUID> {
    boolean existsByCourseIdAndSemesterIdAndSectionCodeIgnoreCase(UUID courseId, UUID semesterId, String sectionCode);
    boolean existsByCourseIdAndSemesterIdAndSectionCodeIgnoreCaseAndIdNot(UUID courseId, UUID semesterId, String sectionCode, UUID id);
}
