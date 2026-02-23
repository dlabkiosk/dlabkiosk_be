package com.moduletest.deasungkioskbackend.domain.seat.exception;

import com.moduletest.deasungkioskbackend.common.exception.BusinessException;
import com.moduletest.deasungkioskbackend.common.exception.ErrorCode;

public class SeatException extends BusinessException {

    public SeatException(ErrorCode errorCode) {
        super(errorCode);
    }
}
