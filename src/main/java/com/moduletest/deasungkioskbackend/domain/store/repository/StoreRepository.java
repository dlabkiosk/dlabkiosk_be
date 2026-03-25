package com.moduletest.deasungkioskbackend.domain.store.repository;

import com.moduletest.deasungkioskbackend.domain.store.entity.Store;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface StoreRepository extends JpaRepository<Store, Long> {

    Optional<Store> findByStoreCode(String storeCode);

    boolean existsByStoreCode(String storeCode);

    @Query("SELECT s FROM Store s WHERE s.dsaAcadCd IS NOT NULL"
        + " AND s.dsaClientId IS NOT NULL AND s.dsaSecretId IS NOT NULL"
        + " AND s.active = true")
    List<Store> findAllWithDsaCredentials();
}
