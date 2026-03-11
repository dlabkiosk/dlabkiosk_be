package com.moduletest.deasungkioskbackend.domain.seatchangerequest.exception;

import com.moduletest.deasungkioskbackend.common.exception.BusinessException;
import com.moduletest.deasungkioskbackend.common.exception.ErrorCode;

public class SeatChangeRequestException extends BusinessException {

    public SeatChangeRequestException(ErrorCode errorCode) {
        super(errorCode);
    }
}
