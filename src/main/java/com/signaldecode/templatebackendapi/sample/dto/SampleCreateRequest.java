package com.signaldecode.templatebackendapi.sample.dto;

import jakarta.validation.constraints.NotBlank;

public record SampleCreateRequest(
        @NotBlank String name
) {}
