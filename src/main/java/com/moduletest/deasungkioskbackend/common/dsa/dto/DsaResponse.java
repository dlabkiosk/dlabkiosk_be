package com.moduletest.deasungkioskbackend.common.dsa.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DsaResponse {

    private int code;
    private String message;
    private Object data;

    @JsonProperty("total_inwon")
    private String totalInwon;

    @JsonProperty("study_tm")
    private String studyTm;

    private final Map<String, Object> extra = new LinkedHashMap<>();

    @JsonAnySetter
    public void setExtra(String key, Object value) {
        extra.put(key, value);
    }

    public boolean isSuccess() {
        return code == 0;
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getDataAsList() {
        if (data == null) {
            return null;
        }
        if (data instanceof List<?> list) {
            if (list.isEmpty()) {
                return Collections.emptyList();
            }
            if (list.get(0) instanceof Map) {
                return (List<Map<String, Object>>) data;
            }
        }
        return Collections.emptyList();
    }
}
