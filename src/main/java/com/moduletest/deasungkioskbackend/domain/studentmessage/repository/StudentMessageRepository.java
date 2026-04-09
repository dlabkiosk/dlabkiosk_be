package com.moduletest.deasungkioskbackend.domain.studentmessage.repository;

import com.moduletest.deasungkioskbackend.domain.studentmessage.entity.StudentMessage;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StudentMessageRepository extends JpaRepository<StudentMessage, Long> {

    @Query("SELECT sm FROM StudentMessage sm"
        + " JOIN FETCH sm.student"
        + " JOIN FETCH sm.store"
        + " LEFT JOIN FETCH sm.template"
        + " WHERE sm.student.id = :studentId ORDER BY sm.createdAt DESC")
    List<StudentMessage> findAllByStudentId(@Param("studentId") Long studentId);

    @Query("SELECT sm FROM StudentMessage sm"
        + " JOIN FETCH sm.student"
        + " JOIN FETCH sm.store"
        + " LEFT JOIN FETCH sm.template"
        + " WHERE sm.student.id = :studentId AND sm.active = true"
        + " ORDER BY sm.createdAt DESC")
    List<StudentMessage> findAllActiveByStudentId(@Param("studentId") Long studentId);

    @Query("SELECT sm FROM StudentMessage sm"
        + " JOIN FETCH sm.student"
        + " JOIN FETCH sm.store"
        + " LEFT JOIN FETCH sm.template"
        + " WHERE sm.id = :id")
    Optional<StudentMessage> findByIdWithDetails(@Param("id") Long id);

    boolean existsByStudentIdAndTemplateId(Long studentId, Long templateId);

    @Query(value = "SELECT sm FROM StudentMessage sm"
        + " JOIN FETCH sm.student s"
        + " LEFT JOIN FETCH s.assignedSeat"
        + " WHERE sm.template.id = :templateId",
        countQuery = "SELECT COUNT(sm) FROM StudentMessage sm WHERE sm.template.id = :templateId")
    Page<StudentMessage> findAllByTemplateIdWithStudent(
        @Param("templateId") Long templateId,
        Pageable pageable);
}
