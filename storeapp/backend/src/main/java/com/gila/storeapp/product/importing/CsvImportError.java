package com.gila.storeapp.product.importing;

public record CsvImportError(int row, String sku, String message) {
}
