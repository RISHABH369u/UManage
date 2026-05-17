package com.umanage.academic.repository;

import com.umanage.academic.entity.Semester;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SemesterRepository extends JpaRepository<Semester, UUID> {
    boolean existsByProgramIdAndSequenceNumber(UUID programId, int sequenceNumber);
    boolean existsByProgramIdAndSequenceNumberAndIdNot(UUID programId, int sequenceNumber, UUID id);
}
