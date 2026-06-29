package com.gila.storeapp.purchase;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record PurchaseRequest(@NotEmpty List<@Valid PurchaseLineRequest> items) {
    public record PurchaseLineRequest(@NotNull Long productId, @Min(1) int quantity) {
    }
}
