package com.umanage.exam.repository;

import com.umanage.exam.entity.Exam;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ExamRepository extends JpaRepository<Exam, UUID> {
}
