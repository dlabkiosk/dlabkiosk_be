package com.moduletest.deasungkioskbackend.domain.student.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.moduletest.deasungkioskbackend.common.exception.ErrorCode;
import com.moduletest.deasungkioskbackend.domain.student.exception.StudentException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public final class QrCodeService {

    private static final int QR_SIZE = 300;
    private static final String QR_FORMAT = "PNG";

    public byte[] generateQrCodePng(String qrUuid) {
        try {
            Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            hints.put(EncodeHintType.MARGIN, 2);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(
                qrUuid, BarcodeFormat.QR_CODE, QR_SIZE, QR_SIZE, hints
            );

            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                MatrixToImageWriter.writeToStream(bitMatrix, QR_FORMAT, outputStream);
                return outputStream.toByteArray();
            }
        } catch (WriterException | IOException e) {
            throw new StudentException(ErrorCode.QR_CODE_GENERATION_FAILED);
        }
    }
}
