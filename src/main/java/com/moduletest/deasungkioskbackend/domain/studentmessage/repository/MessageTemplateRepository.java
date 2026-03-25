package com.moduletest.deasungkioskbackend.domain.studentmessage.repository;

import com.moduletest.deasungkioskbackend.domain.studentmessage.entity.MessageTemplate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MessageTemplateRepository extends JpaRepository<MessageTemplate, Long> {

    @Query("SELECT mt FROM MessageTemplate mt JOIN FETCH mt.store"
        + " WHERE mt.store.id = :storeId ORDER BY mt.createdAt DESC")
    List<MessageTemplate> findAllByStoreIdWithStore(@Param("storeId") Long storeId);

    @Query("SELECT mt FROM MessageTemplate mt JOIN FETCH mt.store"
        + " ORDER BY mt.createdAt DESC")
    List<MessageTemplate> findAllWithStore();

    @Query("SELECT mt FROM MessageTemplate mt JOIN FETCH mt.store WHERE mt.id = :id")
    Optional<MessageTemplate> findByIdWithStore(@Param("id") Long id);
}
