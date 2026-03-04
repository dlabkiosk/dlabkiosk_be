package com.moduletest.deasungkioskbackend.domain.phonesubmission.repository;

import com.moduletest.deasungkioskbackend.domain.phonesubmission.entity.PhoneSubmission;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PhoneSubmissionRepository extends JpaRepository<PhoneSubmission, Long> {


    @Query("SELECT ps FROM PhoneSubmission ps "
        + "JOIN FETCH ps.student s LEFT JOIN FETCH s.assignedSeat "
        + "WHERE ps.store.id = :storeId "
        + "AND ps.submittedAt >= :startOfDay "
        + "ORDER BY ps.submittedAt DESC")
    List<PhoneSubmission> findAllByStoreIdToday(
        @Param("storeId") Long storeId,
        @Param("startOfDay") LocalDateTime startOfDay);

    boolean existsByStudentIdAndSubmittedAtGreaterThanEqual(
        Long studentId, LocalDateTime startOfDay);

}
