package com.moduletest.deasungkioskbackend.domain.attendance.exception;

import com.moduletest.deasungkioskbackend.common.exception.BusinessException;
import com.moduletest.deasungkioskbackend.common.exception.ErrorCode;

public final class AttendanceException extends BusinessException {

    public AttendanceException(ErrorCode errorCode) {
        super(errorCode);
    }
}
