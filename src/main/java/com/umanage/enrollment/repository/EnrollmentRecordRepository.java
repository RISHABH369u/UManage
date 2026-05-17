package com.umanage.enrollment.repository;

import com.umanage.enrollment.entity.EnrollmentRecord;
import com.umanage.enrollment.entity.EnrollmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EnrollmentRecordRepository extends JpaRepository<EnrollmentRecord, UUID> {
    Optional<EnrollmentRecord> findByStudentUserIdAndCourseOfferingId(UUID studentUserId, UUID courseOfferingId);

    boolean existsByStudentUserIdAndCourseOfferingIdAndStatus(UUID studentUserId,
                                                              UUID courseOfferingId,
                                                              EnrollmentStatus status);

    long countByCourseOfferingIdAndStatus(UUID courseOfferingId, EnrollmentStatus status);

    List<EnrollmentRecord> findByStudentUserIdAndStatusOrderByUpdatedAtDesc(UUID studentUserId, EnrollmentStatus status);
}
