package com.moduletest.deasungkioskbackend.domain.studentmessage.exception;

import com.moduletest.deasungkioskbackend.common.exception.BusinessException;
import com.moduletest.deasungkioskbackend.common.exception.ErrorCode;

public class StudentMessageException extends BusinessException {

    public StudentMessageException(ErrorCode errorCode) {
        super(errorCode);
    }
}
