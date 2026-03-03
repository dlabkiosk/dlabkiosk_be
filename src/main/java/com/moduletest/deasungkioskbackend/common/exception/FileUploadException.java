package com.moduletest.deasungkioskbackend.common.exception;

public class FileUploadException extends BusinessException {

    public FileUploadException(ErrorCode errorCode) {
        super(errorCode);
    }
}
