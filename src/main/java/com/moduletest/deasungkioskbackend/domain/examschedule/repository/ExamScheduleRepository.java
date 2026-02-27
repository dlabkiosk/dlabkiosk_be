package com.moduletest.deasungkioskbackend.domain.examschedule.repository;

import com.moduletest.deasungkioskbackend.domain.examschedule.entity.ExamSchedule;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ExamScheduleRepository extends JpaRepository<ExamSchedule, Long> {

    @Query("SELECT e FROM ExamSchedule e "
        + "JOIN FETCH e.store "
        + "WHERE e.id = :examScheduleId")
    Optional<ExamSchedule> findByIdWithStore(@Param("examScheduleId") Long examScheduleId);

    @Query("SELECT e FROM ExamSchedule e "
        + "JOIN FETCH e.store "
        + "ORDER BY e.examDate ASC")
    List<ExamSchedule> findAllWithStore();

    @Query("SELECT e FROM ExamSchedule e "
        + "JOIN FETCH e.store "
        + "WHERE e.store.id = :storeId "
        + "ORDER BY e.examDate ASC")
    List<ExamSchedule> findAllByStoreIdWithStore(@Param("storeId") Long storeId);
}
