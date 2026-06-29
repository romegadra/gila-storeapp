package com.gila.storeapp.product.importing;

import java.util.List;

public record CsvImportReport(int created, int updated, int skipped, List<CsvImportError> errors) {
}
