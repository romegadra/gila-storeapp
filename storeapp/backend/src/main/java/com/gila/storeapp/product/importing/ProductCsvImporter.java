package com.gila.storeapp.product.importing;

import com.gila.storeapp.product.Product;
import com.gila.storeapp.product.ProductRepository;
import com.gila.storeapp.shared.RetryExecutor;
import com.gila.storeapp.shared.TextSafety;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductCsvImporter {
    private static final List<String> REQUIRED_HEADERS = List.of(
        "name",
        "sku",
        "description",
        "category",
        "price",
        "stock",
        "weight_kg"
    );

    private final ProductRepository productRepository;
    private final RetryExecutor retryExecutor;
    private final TextSafety textSafety;

    public ProductCsvImporter(ProductRepository productRepository, RetryExecutor retryExecutor, TextSafety textSafety) {
        this.productRepository = productRepository;
        this.retryExecutor = retryExecutor;
        this.textSafety = textSafety;
    }

    @Transactional
    public CsvImportReport importCsv(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        String headerLine = reader.readLine();
        if (headerLine == null) {
            return new CsvImportReport(0, 0, 0, List.of(new CsvImportError(1, "", "CSV is empty")));
        }

        List<String> headers = parseLine(headerLine);
        Map<String, Integer> indexes = headerIndexes(headers);
        List<CsvImportError> errors = new ArrayList<>();
        int created = 0;
        int updated = 0;
        int row = 1;
        String line;

        while ((line = reader.readLine()) != null) {
            row++;
            if (line.isBlank()) {
                continue;
            }
            try {
                ProductCsvRow csvRow = toRow(parseLine(line), indexes);
                Product product = productRepository.findBySkuIgnoreCase(csvRow.sku())
                    .orElseGet(Product::new);
                boolean isNew = product.getId() == null;
                product.setName(textSafety.requireSafe("name", csvRow.name()));
                product.setSku(textSafety.requireSafe("sku", csvRow.sku()));
                product.setDescription(textSafety.requireSafe("description", csvRow.description()));
                product.setCategory(textSafety.requireSafe("category", csvRow.category()));
                product.setPrice(csvRow.price());
                product.setStock(csvRow.stock());
                product.setWeightKg(csvRow.weightKg());
                product.setUpdatedAt(Instant.now());
                retryExecutor.execute(() -> productRepository.save(product));
                if (isNew) {
                    created++;
                } else {
                    updated++;
                }
            } catch (RuntimeException ex) {
                errors.add(new CsvImportError(row, safeSku(line, indexes), ex.getMessage()));
            }
        }

        return new CsvImportReport(created, updated, errors.size(), errors);
    }

    private Map<String, Integer> headerIndexes(List<String> headers) {
        Map<String, Integer> indexes = new HashMap<>();
        for (int i = 0; i < headers.size(); i++) {
            indexes.put(headers.get(i).trim().toLowerCase(), i);
        }
        for (String requiredHeader : REQUIRED_HEADERS) {
            if (!indexes.containsKey(requiredHeader)) {
                throw new IllegalArgumentException("Missing required CSV header: " + requiredHeader);
            }
        }
        return indexes;
    }

    private ProductCsvRow toRow(List<String> fields, Map<String, Integer> indexes) {
        String name = required(fields, indexes, "name");
        String sku = required(fields, indexes, "sku");
        String category = required(fields, indexes, "category");
        return new ProductCsvRow(
            name,
            sku,
            value(fields, indexes, "description"),
            category,
            decimal(required(fields, indexes, "price"), "price"),
            integer(required(fields, indexes, "stock"), "stock"),
            decimal(required(fields, indexes, "weight_kg"), "weight_kg")
        );
    }

    private String required(List<String> fields, Map<String, Integer> indexes, String name) {
        String value = value(fields, indexes, name);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " is required");
        }
        return value.trim();
    }

    private String value(List<String> fields, Map<String, Integer> indexes, String name) {
        int index = indexes.get(name);
        if (index >= fields.size()) {
            return "";
        }
        return fields.get(index).trim();
    }

    private BigDecimal decimal(String value, String fieldName) {
        String cleaned = value.trim();
        if (cleaned.startsWith("$")) {
            cleaned = cleaned.substring(1);
        }
        try {
            BigDecimal decimal = new BigDecimal(cleaned);
            if (decimal.signum() < 0) {
                throw new IllegalArgumentException(fieldName + " must be zero or greater");
            }
            return decimal;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(fieldName + " must be a decimal number");
        }
    }

    private int integer(String value, String fieldName) {
        try {
            int parsed = Integer.parseInt(value.trim());
            if (parsed < 0) {
                throw new IllegalArgumentException(fieldName + " must be zero or greater");
            }
            return parsed;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(fieldName + " must be an integer");
        }
    }

    private List<String> parseLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean quoted = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (quoted && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    quoted = !quoted;
                }
            } else if (c == ',' && !quoted) {
                values.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }

        values.add(current.toString());
        return values;
    }

    private String safeSku(String line, Map<String, Integer> indexes) {
        try {
            return value(parseLine(line), indexes, "sku");
        } catch (RuntimeException ex) {
            return "";
        }
    }
}
