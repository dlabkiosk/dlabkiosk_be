package com.moduletest.deasungkioskbackend.domain.mealmenu.exception;

import com.moduletest.deasungkioskbackend.common.exception.BusinessException;
import com.moduletest.deasungkioskbackend.common.exception.ErrorCode;

public final class MealMenuException extends BusinessException {

    public MealMenuException(ErrorCode errorCode) {
        super(errorCode);
    }
}
