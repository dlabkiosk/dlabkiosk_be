package com.moduletest.deasungkioskbackend.domain.kiosk.log;

import org.springframework.data.jpa.repository.JpaRepository;

public interface KioskAuthLogRepository extends JpaRepository<KioskAuthLog, Long> {
}
