package com.signaldecode.templatebackendapi.sample.repository;

import com.signaldecode.templatebackendapi.sample.entity.SampleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SampleRepository extends JpaRepository<SampleEntity, Long> {
}
