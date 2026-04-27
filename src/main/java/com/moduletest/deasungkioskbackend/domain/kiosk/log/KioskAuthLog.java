package com.moduletest.deasungkioskbackend.domain.kiosk.log;

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
@Table(name = "kiosk_auth_logs")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class KioskAuthLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "store_id")
    private Long storeId;

    @Column(name = "store_code", length = 50)
    private String storeCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 30)
    private KioskAuthEventType eventType;

    @Column(name = "reason", length = 50)
    private String reason;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Builder
    public KioskAuthLog(Long storeId, String storeCode, KioskAuthEventType eventType,
                        String reason, String ipAddress, String userAgent) {
        this.storeId = storeId;
        this.storeCode = storeCode;
        this.eventType = eventType;
        this.reason = reason;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.createdAt = LocalDateTime.now();
    }
}
