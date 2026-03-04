package com.moduletest.deasungkioskbackend.domain.phonesubmission.exception;

import com.moduletest.deasungkioskbackend.common.exception.BusinessException;
import com.moduletest.deasungkioskbackend.common.exception.ErrorCode;

public class PhoneSubmissionException extends BusinessException {

    public PhoneSubmissionException(ErrorCode errorCode) {
        super(errorCode);
    }
    
}
