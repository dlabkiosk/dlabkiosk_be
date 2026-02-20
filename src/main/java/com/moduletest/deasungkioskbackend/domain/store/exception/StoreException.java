package com.moduletest.deasungkioskbackend.domain.store.exception;

import com.moduletest.deasungkioskbackend.common.exception.BusinessException;
import com.moduletest.deasungkioskbackend.common.exception.ErrorCode;

public class StoreException extends BusinessException {

    public StoreException(ErrorCode errorCode) {
        super(errorCode);
    }




}
