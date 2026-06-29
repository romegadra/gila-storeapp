package com.gila.storeapp.product.importing;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImportJobRepository extends JpaRepository<ImportJob, Long> {
    Optional<ImportJob> findByIdempotencyKey(String idempotencyKey);
}
