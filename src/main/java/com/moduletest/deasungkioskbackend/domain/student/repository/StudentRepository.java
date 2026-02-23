package com.moduletest.deasungkioskbackend.domain.student.repository;

import com.moduletest.deasungkioskbackend.domain.student.entity.Student;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findByQrUuid(String qrUuid);

    Optional<Student> findByRfidUid(String rfidUid);

    boolean existsByPhone(String phone);

    List<Student> findAllByStoreId(Long storeId);
}
