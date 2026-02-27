package com.moduletest.deasungkioskbackend.domain.advertisement.repository;

import com.moduletest.deasungkioskbackend.domain.advertisement.entity.Advertisement;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AdvertisementRepository extends JpaRepository<Advertisement, Long> {

    @Query("SELECT a FROM Advertisement a JOIN FETCH a.store WHERE a.id = :id")
    Optional<Advertisement> findByIdWithStore(@Param("id") Long id);

    @Query("SELECT a FROM Advertisement a JOIN FETCH a.store ORDER BY a.displayOrder")
    List<Advertisement> findAllWithStore();

    @Query("SELECT a FROM Advertisement a JOIN FETCH a.store "
        + "WHERE a.store.id = :storeId ORDER BY a.displayOrder")
    List<Advertisement> findAllByStoreIdWithStore(@Param("storeId") Long storeId);

    @Query("SELECT a FROM Advertisement a JOIN FETCH a.store "
        + "WHERE a.store.id = :storeId AND a.active = true "
        + "ORDER BY a.displayOrder")
    List<Advertisement> findAllActiveByStoreIdWithStore(@Param("storeId") Long storeId);

}
