package com.gila.storeapp.product.importing;

import java.math.BigDecimal;

record ProductCsvRow(
    String name,
    String sku,
    String description,
    String category,
    BigDecimal price,
    int stock,
    BigDecimal weightKg
) {
}
