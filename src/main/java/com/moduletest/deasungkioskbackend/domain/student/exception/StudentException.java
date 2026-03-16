package com.moduletest.deasungkioskbackend.domain.student.exception;

import com.moduletest.deasungkioskbackend.common.exception.BusinessException;
import com.moduletest.deasungkioskbackend.common.exception.ErrorCode;

public class StudentException extends BusinessException {

    public StudentException(ErrorCode errorCode) {
        super(errorCode);
    }
}
