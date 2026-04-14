package com.moduletest.deasungkioskbackend.domain.noticecategory.repository;

import com.moduletest.deasungkioskbackend.domain.noticecategory.entity.NoticeCategory;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NoticeCategoryRepository extends JpaRepository<NoticeCategory, Long> {

    @Query("SELECT c FROM NoticeCategory c JOIN FETCH c.store"
        + " WHERE c.store.id = :storeId"
        + " ORDER BY c.displayOrder ASC, c.id ASC")
    List<NoticeCategory> findAllByStoreIdOrdered(@Param("storeId") Long storeId);

    @Query("SELECT c FROM NoticeCategory c JOIN FETCH c.store WHERE c.id = :id")
    Optional<NoticeCategory> findByIdWithStore(@Param("id") Long id);

    boolean existsByStoreIdAndName(Long storeId, String name);

    @Query("SELECT COALESCE(MAX(c.displayOrder), 0) FROM NoticeCategory c"
        + " WHERE c.store.id = :storeId")
    int findMaxDisplayOrderByStoreId(@Param("storeId") Long storeId);
}
