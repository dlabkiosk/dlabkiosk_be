package com.signaldecode.templatebackendapi.sample.dto;

import com.signaldecode.templatebackendapi.sample.entity.SampleEntity;

public record SampleResponse(
        Long id,
        String name
) {
    public static SampleResponse from(SampleEntity s) {
        return new SampleResponse(s.getId(), s.getName());
    }
}
