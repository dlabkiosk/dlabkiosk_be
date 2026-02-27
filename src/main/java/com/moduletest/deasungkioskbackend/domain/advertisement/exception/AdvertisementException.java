package com.moduletest.deasungkioskbackend.domain.advertisement.exception;

import com.moduletest.deasungkioskbackend.common.exception.BusinessException;
import com.moduletest.deasungkioskbackend.common.exception.ErrorCode;

public class AdvertisementException extends BusinessException {

    public AdvertisementException(ErrorCode errorCode) {
        super(errorCode);
    }
}
