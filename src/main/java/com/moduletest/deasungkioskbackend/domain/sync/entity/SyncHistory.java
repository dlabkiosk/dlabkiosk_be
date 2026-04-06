package com.moduletest.deasungkioskbackend.domain.sync.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "sync_histories")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SyncHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SyncType syncType;

    @Column
    private Long storeId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SyncStatus status;

    @Column(nullable = false)
    private int totalCount;

    @Column(nullable = false)
    private int createdCount;

    @Column(nullable = false)
    private int updatedCount;

    @Column(nullable = false)
    private int failedCount;

    @Column(length = 1000)
    private String errorMessage;

    @Column(nullable = false)
    private LocalDateTime startedAt;

    @Column
    private LocalDateTime finishedAt;

    @Builder
    public SyncHistory(SyncType syncType, Long storeId, SyncStatus status,
                       int totalCount, int createdCount, int updatedCount,
                       int failedCount, String errorMessage,
                       LocalDateTime startedAt, LocalDateTime finishedAt) {
        this.syncType = syncType;
        this.storeId = storeId;
        this.status = status;
        this.totalCount = totalCount;
        this.createdCount = createdCount;
        this.updatedCount = updatedCount;
        this.failedCount = failedCount;
        this.errorMessage = errorMessage;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
    }
}
