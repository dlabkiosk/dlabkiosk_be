package com.moduletest.deasungkioskbackend.common.dsa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "dsa_anomaly_logs")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DsaAnomalyLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "store_id")
    private Long storeId;

    @Column(name = "rfid_uid", length = 100)
    private String rfidUid;

    @Column(name = "api_path", nullable = false, length = 200)
    private String apiPath;

    @Column(name = "anomaly_type", nullable = false, length = 50)
    private String anomalyType;

    @Column(name = "request_params", columnDefinition = "TEXT")
    private String requestParams;

    @Column(name = "raw_response", columnDefinition = "TEXT")
    private String rawResponse;

    @Column(name = "note", length = 500)
    private String note;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Builder
    public DsaAnomalyLog(Long storeId, String rfidUid, String apiPath, String anomalyType,
                         String requestParams, String rawResponse, String note) {
        this.storeId = storeId;
        this.rfidUid = rfidUid;
        this.apiPath = apiPath;
        this.anomalyType = anomalyType;
        this.requestParams = requestParams;
        this.rawResponse = rawResponse;
        this.note = note;
        this.createdAt = LocalDateTime.now();
    }
}
