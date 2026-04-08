package com.moduletest.deasungkioskbackend.common.dsa.repository;

import com.moduletest.deasungkioskbackend.common.dsa.entity.DsaAnomalyLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DsaAnomalyLogRepository extends JpaRepository<DsaAnomalyLog, Long> {
}
