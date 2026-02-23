package com.moduletest.deasungkioskbackend.domain.outing.exception;

import com.moduletest.deasungkioskbackend.common.exception.BusinessException;
import com.moduletest.deasungkioskbackend.common.exception.ErrorCode;

public final class OutingException extends BusinessException {

    public OutingException(ErrorCode errorCode) {
        super(errorCode);
    }
}
