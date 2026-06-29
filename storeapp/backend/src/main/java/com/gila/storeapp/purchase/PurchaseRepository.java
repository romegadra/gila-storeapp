package com.gila.storeapp.purchase;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
    Optional<Purchase> findByIdempotencyKey(String idempotencyKey);
}
