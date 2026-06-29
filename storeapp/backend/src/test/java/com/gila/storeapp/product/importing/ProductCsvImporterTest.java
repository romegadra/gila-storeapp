package com.gila.storeapp.product.importing;

import static org.assertj.core.api.Assertions.assertThat;

import com.gila.storeapp.product.ProductRepository;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@TestPropertySource(properties = "app.csv.seed-path=missing.csv")
class ProductCsvImporterTest {
    @Autowired
    private ProductCsvImporter importer;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void importsValidRowsAndSkipsInvalidRows() throws Exception {
        String csv = """
            name,sku,description,category,price,stock,weight_kg
            Valid Product,SKU-1,Good row,General,12.50,5,1.2
            Bad Product,SKU-2,Bad row,General,free,5,1.2
            <script>alert('xss')</script>,SKU-4,Bad row,General,12.50,5,1.2
            """;

        CsvImportReport report = importer.importCsv(input(csv));

        assertThat(report.created()).isEqualTo(1);
        assertThat(report.updated()).isZero();
        assertThat(report.skipped()).isEqualTo(2);
        assertThat(report.errors()).hasSize(2);
        assertThat(productRepository.findBySkuIgnoreCase("SKU-1")).isPresent();
        assertThat(productRepository.findBySkuIgnoreCase("SKU-2")).isEmpty();
        assertThat(productRepository.findBySkuIgnoreCase("SKU-4")).isEmpty();
    }

    @Test
    void upsertsProductsBySku() throws Exception {
        String firstCsv = """
            name,sku,description,category,price,stock,weight_kg
            Original,SKU-3,First,General,10.00,3,1.0
            """;
        String secondCsv = """
            name,sku,description,category,price,stock,weight_kg
            Updated,SKU-3,Second,General,14.00,8,1.5
            """;

        CsvImportReport first = importer.importCsv(input(firstCsv));
        CsvImportReport second = importer.importCsv(input(secondCsv));

        assertThat(first.created()).isEqualTo(1);
        assertThat(second.updated()).isEqualTo(1);
        assertThat(productRepository.findBySkuIgnoreCase("SKU-3")).get()
            .satisfies(product -> {
                assertThat(product.getName()).isEqualTo("Updated");
                assertThat(product.getStock()).isEqualTo(8);
            });
        assertThat(productRepository.countBySkuIgnoreCase("SKU-3")).isEqualTo(1);
    }

    private ByteArrayInputStream input(String csv) {
        return new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));
    }
}
