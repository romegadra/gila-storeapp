package com.gila.storeapp.product;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record ProductRequest(
    @NotBlank String name,
    @NotBlank String sku,
    String description,
    @NotBlank String category,
    @NotNull @DecimalMin("0.00") BigDecimal price,
    @Min(0) int stock,
    @NotNull @DecimalMin("0.00") BigDecimal weightKg
) {
}
