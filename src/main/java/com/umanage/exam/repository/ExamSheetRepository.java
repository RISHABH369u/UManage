package com.umanage.exam.repository;

import com.umanage.exam.entity.ExamSheet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExamSheetRepository extends JpaRepository<ExamSheet, UUID> {
    void deleteByExamId(UUID examId);

    Optional<ExamSheet> findBySheetId(String sheetId);

    List<ExamSheet> findByExamIdOrderByCreatedAtAsc(UUID examId);
}
