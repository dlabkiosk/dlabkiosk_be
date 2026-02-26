package com.moduletest.deasungkioskbackend.domain.notice.exception;

import com.moduletest.deasungkioskbackend.common.exception.BusinessException;
import com.moduletest.deasungkioskbackend.common.exception.ErrorCode;

public class NoticeException extends BusinessException {

    public NoticeException(ErrorCode errorCode) {
        super(errorCode);
    }
}
