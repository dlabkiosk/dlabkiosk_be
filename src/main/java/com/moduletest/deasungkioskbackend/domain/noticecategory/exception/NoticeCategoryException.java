package com.moduletest.deasungkioskbackend.domain.noticecategory.exception;

import com.moduletest.deasungkioskbackend.common.exception.BusinessException;
import com.moduletest.deasungkioskbackend.common.exception.ErrorCode;

public class NoticeCategoryException extends BusinessException {

    public NoticeCategoryException(ErrorCode errorCode) {
        super(errorCode);
    }
}
