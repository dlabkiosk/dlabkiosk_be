package com.moduletest.deasungkioskbackend.domain.examschedule.exception;

import com.moduletest.deasungkioskbackend.common.exception.BusinessException;
import com.moduletest.deasungkioskbackend.common.exception.ErrorCode;

public class ExamScheduleException extends BusinessException {

    public ExamScheduleException(ErrorCode errorCode) {
        super(errorCode);
    }
}
