package com.moduletest.deasungkioskbackend.domain.seatleave.exception;

import com.moduletest.deasungkioskbackend.common.exception.BusinessException;
import com.moduletest.deasungkioskbackend.common.exception.ErrorCode;

public class SeatLeaveException extends BusinessException {

    public SeatLeaveException(ErrorCode errorCode) {
        super(errorCode);
    }

}
