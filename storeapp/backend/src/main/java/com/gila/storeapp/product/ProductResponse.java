package com.gila.storeapp.product;

import java.math.BigDecimal;
import java.time.Instant;

public record ProductResponse(
    Long id,
    String name,
    String sku,
    String description,
    String category,
    BigDecimal price,
    int stock,
    BigDecimal weightKg,
    Instant createdAt,
    Instant updatedAt
) {
    static ProductResponse from(Product product) {
        return new ProductResponse(
            product.getId(),
            product.getName(),
            product.getSku(),
            product.getDescription(),
            product.getCategory(),
            product.getPrice(),
            product.getStock(),
            product.getWeightKg(),
            product.getCreatedAt(),
            product.getUpdatedAt()
        );
    }
}
