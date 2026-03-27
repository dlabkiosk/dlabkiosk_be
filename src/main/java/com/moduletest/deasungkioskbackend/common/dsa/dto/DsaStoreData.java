package com.moduletest.deasungkioskbackend.common.dsa.dto;

import java.util.Map;

public record DsaStoreData(
    String acadCd,
    String acadNm,
    String fullNm
) {

    public static DsaStoreData fromMap(Map<String, Object> map) {
        return new DsaStoreData(
            getStringValue(map, "acad_cd"),
            getStringValue(map, "acad_nm"),
            getStringValue(map, "full_nm")
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
