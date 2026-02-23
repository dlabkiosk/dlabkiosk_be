package com.moduletest.deasungkioskbackend.domain.kiosk.exception;

import com.moduletest.deasungkioskbackend.common.exception.BusinessException;
import com.moduletest.deasungkioskbackend.common.exception.ErrorCode;

public final class KioskException extends BusinessException {

    public KioskException(ErrorCode errorCode) {
        super(errorCode);
    }
}
