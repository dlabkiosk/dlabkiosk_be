package com.moduletest.deasungkioskbackend.domain.notice.repository;

import com.moduletest.deasungkioskbackend.domain.notice.entity.Notice;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

    @Query("SELECT n FROM Notice n "
        + "JOIN FETCH n.store "
        + "WHERE n.id = :noticeId")
    Optional<Notice> findByIdWithStore(@Param("noticeId") Long noticeId);

    @Query("SELECT n FROM Notice n "
        + "JOIN FETCH n.store "
        + "ORDER BY n.pinned DESC, n.createdAt DESC")
    List<Notice> findAllWithStore();

    @Query("SELECT n FROM Notice n "
        + "JOIN FETCH n.store "
        + "WHERE n.store.id = :storeId "
        + "ORDER BY n.pinned DESC, n.createdAt DESC")
    List<Notice> findAllByStoreIdWithStore(@Param("storeId") Long storeId);

    @Query("SELECT n FROM Notice n "
        + "JOIN FETCH n.store "
        + "WHERE n.store.id = :storeId "
        + "AND n.active = true "
        + "ORDER BY n.pinned DESC, n.createdAt DESC")
    List<Notice> findAllActiveByStoreIdWithStore(@Param("storeId") Long storeId);
}
