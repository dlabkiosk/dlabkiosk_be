package com.moduletest.deasungkioskbackend.common.service;

import com.moduletest.deasungkioskbackend.common.exception.BusinessException;
import com.moduletest.deasungkioskbackend.common.exception.ErrorCode;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
public final class ImageCropService {

    public InputStream cropImage(MultipartFile file, int cropX, int cropY,
                                  int cropWidth, int cropHeight) {
        try {
            BufferedImage original = ImageIO.read(file.getInputStream());
            if (original == null) {
                throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED);
            }

            validateCropBounds(original, cropX, cropY, cropWidth, cropHeight);

            BufferedImage cropped = original.getSubimage(cropX, cropY, cropWidth, cropHeight);

            String format = extractFormat(file.getOriginalFilename());
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(cropped, format, out);

            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            log.error("이미지 크롭 실패", e);
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    public long getCroppedSize(MultipartFile file, int cropX, int cropY,
                                int cropWidth, int cropHeight) {
        try {
            BufferedImage original = ImageIO.read(file.getInputStream());
            if (original == null) {
                throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED);
            }

            validateCropBounds(original, cropX, cropY, cropWidth, cropHeight);

            BufferedImage cropped = original.getSubimage(cropX, cropY, cropWidth, cropHeight);

            String format = extractFormat(file.getOriginalFilename());
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(cropped, format, out);

            return out.size();
        } catch (IOException e) {
            log.error("이미지 크롭 사이즈 계산 실패", e);
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    private void validateCropBounds(BufferedImage image, int x, int y, int w, int h) {
        if (x < 0 || y < 0 || w <= 0 || h <= 0
            || x + w > image.getWidth()
            || y + h > image.getHeight()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private String extractFormat(String filename) {
        if (filename != null && filename.contains(".")) {
            String ext = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
            if ("jpg".equals(ext) || "jpeg".equals(ext)) {
                return "jpg";
            }
            if ("png".equals(ext)) {
                return "png";
            }
            if ("gif".equals(ext)) {
                return "gif";
            }
        }
        return "png";
    }
}
