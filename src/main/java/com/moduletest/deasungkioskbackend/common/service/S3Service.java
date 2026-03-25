package com.moduletest.deasungkioskbackend.common.service;


import com.moduletest.deasungkioskbackend.common.exception.ErrorCode;
import com.moduletest.deasungkioskbackend.common.exception.FileUploadException;
import java.io.InputStream;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
public class S3Service {

    private final S3Client s3Client;
    private final String bucket;
    private final String region;

    public S3Service(
        S3Client s3Client,
        @Value("${cloud.aws.s3.bucket}") String bucket,
        @Value("${cloud.aws.region.static}") String region) {
        this.s3Client = s3Client;
        this.bucket = bucket;
        this.region = region;
    }

    public String upload(MultipartFile file) {

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String key = UUID.randomUUID() + extension;

        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(file.getContentType())
                .build();

            s3Client.putObject(
                putRequest,
                RequestBody.fromInputStream(file.getInputStream(), file.getSize())
            );

        } catch (Exception e) {
            throw new FileUploadException(ErrorCode.FILE_UPLOAD_FAILED);
        }

        return "https://" + bucket + ".s3." + region + ".amazonaws.com/" + key;
    }

    public String upload(InputStream inputStream, long contentLength,
                         String contentType, String extension) {
        String key = UUID.randomUUID() + extension;

        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .build();

            s3Client.putObject(
                putRequest,
                RequestBody.fromInputStream(inputStream, contentLength)
            );
        } catch (Exception e) {
            throw new FileUploadException(ErrorCode.FILE_UPLOAD_FAILED);
        }

        return "https://" + bucket + ".s3." + region + ".amazonaws.com/" + key;
    }

    public void delete(String fileUrl) {
        if (fileUrl == null || !fileUrl.contains(".amazonaws.com/")) {
            return;
        }

        String key = fileUrl.substring(fileUrl.indexOf(".amazonaws.com/") + 15);

        try {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

            s3Client.deleteObject(deleteRequest);
        } catch (Exception e) {
            throw new FileUploadException(ErrorCode.FILE_DELETE_FAILED);
        }
    }

}
