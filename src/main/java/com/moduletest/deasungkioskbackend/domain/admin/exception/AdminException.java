package com.moduletest.deasungkioskbackend.domain.admin.exception;

import com.moduletest.deasungkioskbackend.common.exception.BusinessException;
import com.moduletest.deasungkioskbackend.common.exception.ErrorCode;

public class AdminException extends BusinessException {

    public AdminException(ErrorCode errorCode) {
        super(errorCode);
    }
}
