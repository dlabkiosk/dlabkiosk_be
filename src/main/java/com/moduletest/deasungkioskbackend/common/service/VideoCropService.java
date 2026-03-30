package com.moduletest.deasungkioskbackend.common.service;

import com.moduletest.deasungkioskbackend.common.exception.BusinessException;
import com.moduletest.deasungkioskbackend.common.exception.ErrorCode;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
public final class VideoCropService {

    public CropResult cropVideo(MultipartFile file, int cropX, int cropY,
                                int cropWidth, int cropHeight) {
        File inputFile = null;
        File outputFile = null;

        try {
            String extension = extractExtension(file.getOriginalFilename());
            inputFile = File.createTempFile("video-input-", extension);
            outputFile = File.createTempFile("video-output-", extension);
            file.transferTo(inputFile);

            String cropFilter = String.format("crop=%d:%d:%d:%d",
                cropWidth, cropHeight, cropX, cropY);

            ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg", "-y",
                "-i", inputFile.getAbsolutePath(),
                "-vf", cropFilter,
                "-c:a", "copy",
                outputFile.getAbsolutePath()
            );
            pb.redirectErrorStream(true);

            Process process = pb.start();
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                String errorOutput = new String(process.getInputStream().readAllBytes());
                log.error("FFmpeg 크롭 실패 (exitCode={}): {}", exitCode, errorOutput);
                throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED);
            }

            byte[] result = Files.readAllBytes(outputFile.toPath());
            return new CropResult(new java.io.ByteArrayInputStream(result), result.length);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("동영상 크롭 실패", e);
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED);
        } finally {
            deleteQuietly(inputFile);
            deleteQuietly(outputFile);
        }
    }

    public record CropResult(InputStream inputStream, long size) {

    }

    private String extractExtension(String filename) {
        if (filename != null && filename.contains(".")) {
            return filename.substring(filename.lastIndexOf("."));
        }
        return ".mp4";
    }

    private void deleteQuietly(File file) {
        if (file != null && file.exists()) {
            file.delete();
        }
    }
}
