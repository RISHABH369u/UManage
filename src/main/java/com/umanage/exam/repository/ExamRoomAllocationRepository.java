package com.umanage.exam.repository;

import com.umanage.exam.entity.ExamRoomAllocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ExamRoomAllocationRepository extends JpaRepository<ExamRoomAllocation, UUID> {
    List<ExamRoomAllocation> findByExamIdOrderByDisplayOrderAsc(UUID examId);

    void deleteByExamId(UUID examId);
}
