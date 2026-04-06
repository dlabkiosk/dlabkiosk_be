package com.moduletest.deasungkioskbackend.domain.sync.repository;

import com.moduletest.deasungkioskbackend.domain.sync.entity.SyncHistory;
import com.moduletest.deasungkioskbackend.domain.sync.entity.SyncType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SyncHistoryRepository extends JpaRepository<SyncHistory, Long> {

    Optional<SyncHistory> findTopBySyncTypeOrderByStartedAtDesc(SyncType syncType);
}
