package com.umanage.academic.repository;

import com.umanage.academic.entity.Program;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProgramRepository extends JpaRepository<Program, UUID> {
    Optional<Program> findByCodeIgnoreCase(String code);
    boolean existsByCodeIgnoreCase(String code);
}
