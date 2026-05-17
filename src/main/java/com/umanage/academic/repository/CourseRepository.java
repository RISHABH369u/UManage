package com.umanage.academic.repository;

import com.umanage.academic.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CourseRepository extends JpaRepository<Course, UUID> {
    Optional<Course> findByCodeIgnoreCase(String code);
    boolean existsByCodeIgnoreCase(String code);
}
