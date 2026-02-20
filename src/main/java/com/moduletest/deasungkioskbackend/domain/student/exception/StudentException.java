package com.moduletest.deasungkioskbackend.domain.student.exception;

import com.moduletest.deasungkioskbackend.common.exception.BusinessException;
import com.moduletest.deasungkioskbackend.common.exception.ErrorCode;

public final class StudentException extends BusinessException {

    public StudentException(ErrorCode errorCode) {
        super(errorCode);
    }
}
