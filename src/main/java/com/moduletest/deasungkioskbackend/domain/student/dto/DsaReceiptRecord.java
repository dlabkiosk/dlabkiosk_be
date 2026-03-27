package com.moduletest.deasungkioskbackend.domain.student.dto;

import lombok.Builder;

@Builder
public record DsaReceiptRecord(
    String receiptName,
    String suppliedAmount,
    String receivedAmount,
    String unpaidAmount
) {
}
