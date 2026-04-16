package com.moduletest.deasungkioskbackend.domain.student.repository;

import com.moduletest.deasungkioskbackend.domain.student.entity.Student;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StudentRepository extends JpaRepository<Student, Long> {

    @Query("SELECT s FROM Student s WHERE s.rfidUid = :rfidUid AND s.active = true")
    Optional<Student> findByRfidUid(@Param("rfidUid") String rfidUid);

    Optional<Student> findByStudentNumber(String studentNumber);

    @Query("SELECT s FROM Student s JOIN FETCH s.store LEFT JOIN FETCH s.assignedSeat"
        + " WHERE s.store.id = :storeId")
    List<Student> findAllByStoreIdWithStore(@Param("storeId") Long storeId);

    @Query("SELECT s FROM Student s JOIN FETCH s.store LEFT JOIN FETCH s.assignedSeat")
    List<Student> findAllWithStore();

    @Query("SELECT s FROM Student s JOIN FETCH s.store LEFT JOIN FETCH s.assignedSeat"
        + " WHERE s.store.id = :storeId AND s.active = :active")
    List<Student> findAllByStoreIdAndActive(
        @Param("storeId") Long storeId,
        @Param("active") boolean active);

    @Query("SELECT s FROM Student s JOIN FETCH s.store LEFT JOIN FETCH s.assignedSeat"
        + " WHERE s.active = :active")
    List<Student> findAllByActive(@Param("active") boolean active);

    @Query("SELECT s FROM Student s JOIN FETCH s.store LEFT JOIN FETCH s.assignedSeat"
        + " WHERE s.id = :id")
    Optional<Student> findByIdWithStore(@Param("id") Long id);

    @Query("SELECT s FROM Student s JOIN FETCH s.store LEFT JOIN FETCH s.assignedSeat"
        + " WHERE s.id IN :ids")
    List<Student> findAllByIdsWithStore(@Param("ids") List<Long> ids);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Student s WHERE s.id = :id")
    Optional<Student> findByIdForUpdate(@Param("id") Long id);

    @Query("SELECT s FROM Student s JOIN FETCH s.store LEFT JOIN FETCH s.assignedSeat"
        + " WHERE s.assignedSeat.seatLabel = :seatLabel AND s.store.id = :storeId"
        + " AND s.active = true")
    Optional<Student> findBySeatLabelAndStoreId(
        @Param("seatLabel") String seatLabel,
        @Param("storeId") Long storeId);

    @Query("SELECT s FROM Student s JOIN FETCH s.store LEFT JOIN FETCH s.assignedSeat"
        + " WHERE s.phoneLast4 = :phoneLast4 AND s.store.id = :storeId"
        + " AND s.active = true")
    List<Student> findAllByPhoneLast4AndStoreId(
        @Param("phoneLast4") String phoneLast4,
        @Param("storeId") Long storeId);

    @Query("SELECT s FROM Student s JOIN FETCH s.store LEFT JOIN FETCH s.assignedSeat"
        + " WHERE s.phone = :phone AND s.store.id = :storeId"
        + " AND s.active = true")
    List<Student> findAllByPhoneAndStoreId(
        @Param("phone") String phone,
        @Param("storeId") Long storeId);

    boolean existsByStudentNumber(String studentNumber);

    @Query("SELECT COUNT(s) > 0 FROM Student s"
        + " WHERE s.rfidUid = :rfidUid AND s.store.id <> :storeId")
    boolean existsByRfidUidAndStoreIdNot(
        @Param("rfidUid") String rfidUid,
        @Param("storeId") Long storeId);

    boolean existsByAssignedSeatId(Long assignedSeatId);

    boolean existsByAssignedSeatIdAndIdNot(Long assignedSeatId, Long studentId);

    long countByStoreId(Long storeId);

    @Query("SELECT s FROM Student s"
        + " WHERE s.store.id = :storeId AND s.assignedSeat IS NOT NULL")
    List<Student> findAllWithAssignedSeatByStoreId(@Param("storeId") Long storeId);

    @Query("SELECT s FROM Student s JOIN FETCH s.store LEFT JOIN FETCH s.assignedSeat"
        + " WHERE s.store.id = :storeId"
        + " AND (:name IS NULL OR s.name LIKE %:name%)"
        + " AND (:studentNumber IS NULL OR s.studentNumber = :studentNumber)")
    List<Student> findAllByStoreIdAndFilter(
        @Param("storeId") Long storeId,
        @Param("name") String name,
        @Param("studentNumber") String studentNumber);

    List<Student> findAllByStoreId(Long storeId);

    @Query(value = "SELECT s FROM Student s"
        + " LEFT JOIN FETCH s.assignedSeat"
        + " WHERE s.store.id = :storeId"
        + " AND s.id NOT IN ("
        + "   SELECT sm.student.id FROM StudentMessage sm"
        + "   WHERE sm.template.id = :templateId"
        + " )",
        countQuery = "SELECT COUNT(s) FROM Student s"
            + " WHERE s.store.id = :storeId"
            + " AND s.id NOT IN ("
            + "   SELECT sm.student.id FROM StudentMessage sm"
            + "   WHERE sm.template.id = :templateId"
            + " )")
    Page<Student> findEligibleStudentsForTemplate(
        @Param("storeId") Long storeId,
        @Param("templateId") Long templateId,
        Pageable pageable);
}
