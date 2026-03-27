package com.moduletest.deasungkioskbackend.common.dsa.dto;

import java.util.Map;

public record DsaStudentData(
    String stdNm,
    String stdNo,
    String rfidNo,
    String hp,
    String seatCd
) {

    public static DsaStudentData fromMap(Map<String, Object> map) {
        return new DsaStudentData(
            getStringValue(map, "std_nm"),
            getStringValue(map, "std_no"),
            getStringValue(map, "rfid_no"),
            getStringValue(map, "hp"),
            getStringValue(map, "seat_cd")
        );
    }

    private static String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null || "null".equals(String.valueOf(value))) {
            return null;
        }
        return String.valueOf(value).trim();
    }
}
