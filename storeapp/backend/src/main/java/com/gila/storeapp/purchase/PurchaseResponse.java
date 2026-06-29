package com.gila.storeapp.purchase;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record PurchaseResponse(
    Long id,
    String status,
    BigDecimal total,
    Instant purchasedAt,
    List<PurchaseLineResponse> items
) {
    static PurchaseResponse from(Purchase purchase) {
        return new PurchaseResponse(
            purchase.getId(),
            purchase.getStatus(),
            purchase.getTotal(),
            purchase.getPurchasedAt(),
            purchase.getItems().stream().map(PurchaseLineResponse::from).toList()
        );
    }

    public record PurchaseLineResponse(
        Long productId,
        String productName,
        String sku,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal lineTotal
    ) {
        static PurchaseLineResponse from(PurchaseItem item) {
            return new PurchaseLineResponse(
                item.getProductId(),
                item.getProductName(),
                item.getSku(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getLineTotal()
            );
        }
    }
}
