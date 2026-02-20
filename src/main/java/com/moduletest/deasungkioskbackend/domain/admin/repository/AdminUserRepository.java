package com.moduletest.deasungkioskbackend.domain.admin.repository;

import com.moduletest.deasungkioskbackend.domain.admin.entity.AdminUser;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminUserRepository extends JpaRepository<AdminUser, Long> {

    Optional<AdminUser> findByLoginId(String loginId);

}
