package com.signaldecode.templatebackendapi.sample.controller;

import com.signaldecode.templatebackendapi.common.response.CommonResponse;
import com.signaldecode.templatebackendapi.sample.dto.SampleCreateRequest;
import com.signaldecode.templatebackendapi.sample.dto.SampleResponse;
import com.signaldecode.templatebackendapi.sample.service.SampleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/samples")
public class SampleController {

    private final SampleService service;

    @PostMapping
    public CommonResponse<SampleResponse> create(@Valid @RequestBody SampleCreateRequest req) {
        return CommonResponse.success(SampleResponse.from(service.create(req)));
    }

    @GetMapping
    public CommonResponse<List<SampleResponse>> findAll() {
        return CommonResponse.success(
            service.findAll().stream().map(SampleResponse::from).toList()
        );
    }

    @GetMapping("/{id}")
    public CommonResponse<SampleResponse> findById(@PathVariable Long id) {
        return CommonResponse.success(SampleResponse.from(service.findById(id)));
    }
}
