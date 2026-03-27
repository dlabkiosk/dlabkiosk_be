package com.moduletest.deasungkioskbackend.domain.tag.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AttendAction {

    S("등원"),
    T("하원"),
    A("지각"),
    D("외출"),
    N("사유외출"),
    C("조퇴"),
    R("복귀");

    private final String label;

    public static AttendAction fromCode(String code) {
        for (AttendAction action : values()) {
            if (action.name().equals(code)) {
                return action;
            }
        }
        throw new IllegalArgumentException("알 수 없는 출결 구분: " + code);
    }
}
