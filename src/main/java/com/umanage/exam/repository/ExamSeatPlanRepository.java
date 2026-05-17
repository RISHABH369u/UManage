package com.umanage.exam.repository;

import com.umanage.exam.entity.ExamSeatPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ExamSeatPlanRepository extends JpaRepository<ExamSeatPlan, UUID> {
    List<ExamSeatPlan> findByExamIdOrderBySeatNumberAsc(UUID examId);

    void deleteByExamId(UUID examId);
}
