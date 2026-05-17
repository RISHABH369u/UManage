package com.umanage.exam.repository;

import com.umanage.exam.entity.ExamInvigilatorAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ExamInvigilatorAssignmentRepository extends JpaRepository<ExamInvigilatorAssignment, UUID> {
    void deleteByExamId(UUID examId);
}
