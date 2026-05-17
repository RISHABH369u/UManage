package com.umanage.academic.repository;

import com.umanage.academic.entity.Prerequisite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PrerequisiteRepository extends JpaRepository<Prerequisite, UUID> {
    boolean existsByCourseIdAndPrerequisiteCourseId(UUID courseId, UUID prerequisiteCourseId);
    boolean existsByCourseIdAndPrerequisiteCourseIdAndIdNot(UUID courseId, UUID prerequisiteCourseId, UUID id);
}
