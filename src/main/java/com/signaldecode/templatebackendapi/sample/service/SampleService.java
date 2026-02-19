package com.signaldecode.templatebackendapi.sample.service;

import com.signaldecode.templatebackendapi.common.exception.SampleException;
import com.signaldecode.templatebackendapi.sample.dto.SampleCreateRequest;
import com.signaldecode.templatebackendapi.sample.entity.SampleEntity;
import com.signaldecode.templatebackendapi.sample.repository.SampleRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class SampleService {

    private final SampleRepository sampleRepository;

    @Transactional
    public SampleEntity create(SampleCreateRequest sampleCreateRequest) {
        SampleEntity saved = sampleRepository.save(new SampleEntity(sampleCreateRequest.name()));
        return saved;
    }

    @Transactional(readOnly = true)
    public List<SampleEntity> findAll() {
        return sampleRepository.findAll();
    }

    @Transactional(readOnly = true)
    public SampleEntity findById(Long id) {
        return sampleRepository.findById(id)
                .orElseThrow(SampleException::invalidInput);
    }
}
