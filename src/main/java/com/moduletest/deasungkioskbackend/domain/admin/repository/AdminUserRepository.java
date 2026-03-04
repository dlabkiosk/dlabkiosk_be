package com.moduletest.deasungkioskbackend.domain.admin.repository;

import com.moduletest.deasungkioskbackend.domain.admin.entity.AdminUser;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AdminUserRepository extends JpaRepository<AdminUser, Long> {

    Optional<AdminUser> findByLoginId(String loginId);

    @Query("SELECT a FROM AdminUser a JOIN FETCH a.store WHERE a.id = :id")
    Optional<AdminUser> findByIdWithStore(@Param("id") Long id);

    @Query("SELECT a FROM AdminUser a JOIN FETCH a.store WHERE a.loginId = :loginId")
    Optional<AdminUser> findByLoginIdWithStore(@Param("loginId") String loginId);

    @Query("SELECT a FROM AdminUser a JOIN FETCH a.store ORDER BY a.id ASC")
    List<AdminUser> findAllWithStore();

}
