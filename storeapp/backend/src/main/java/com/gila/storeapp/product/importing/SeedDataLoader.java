package com.gila.storeapp.product.importing;

import java.io.InputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component
public class SeedDataLoader implements ApplicationRunner {
    private final ProductCsvImporter productCsvImporter;
    private final String seedPath;

    public SeedDataLoader(ProductCsvImporter productCsvImporter, @Value("${app.csv.seed-path}") String seedPath) {
        this.productCsvImporter = productCsvImporter;
        this.seedPath = seedPath;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        ClassPathResource resource = new ClassPathResource(seedPath);
        if (resource.exists()) {
            try (InputStream inputStream = resource.getInputStream()) {
                productCsvImporter.importCsv(inputStream);
            }
        }
    }
}
