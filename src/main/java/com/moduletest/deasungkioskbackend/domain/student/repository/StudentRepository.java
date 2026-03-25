package com.moduletest.deasungkioskbackend.domain.student.repository;

import com.moduletest.deasungkioskbackend.domain.student.entity.Student;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findByRfidUid(String rfidUid);

    Optional<Student> findByStudentNumber(String studentNumber);

    @Query("SELECT s FROM Student s JOIN FETCH s.store LEFT JOIN FETCH s.assignedSeat"
        + " WHERE s.store.id = :storeId")
    List<Student> findAllByStoreIdWithStore(@Param("storeId") Long storeId);

    @Query("SELECT s FROM Student s JOIN FETCH s.store LEFT JOIN FETCH s.assignedSeat")
    List<Student> findAllWithStore();

    @Query("SELECT s FROM Student s JOIN FETCH s.store LEFT JOIN FETCH s.assignedSeat"
        + " WHERE s.id = :id")
    Optional<Student> findByIdWithStore(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Student s WHERE s.id = :id")
    Optional<Student> findByIdForUpdate(@Param("id") Long id);

    @Query("SELECT s FROM Student s JOIN FETCH s.store LEFT JOIN FETCH s.assignedSeat"
        + " WHERE s.assignedSeat.seatLabel = :seatLabel AND s.store.id = :storeId")
    Optional<Student> findBySeatLabelAndStoreId(
        @Param("seatLabel") String seatLabel,
        @Param("storeId") Long storeId);

    @Query("SELECT s FROM Student s JOIN FETCH s.store LEFT JOIN FETCH s.assignedSeat"
        + " WHERE s.phoneLast4 = :phoneLast4 AND s.store.id = :storeId")
    List<Student> findAllByPhoneLast4AndStoreId(
        @Param("phoneLast4") String phoneLast4,
        @Param("storeId") Long storeId);

    boolean existsByStudentNumber(String studentNumber);

    boolean existsByAssignedSeatId(Long assignedSeatId);

    long countByStoreId(Long storeId);
}
