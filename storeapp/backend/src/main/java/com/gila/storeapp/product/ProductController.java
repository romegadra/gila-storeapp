package com.gila.storeapp.product;

import com.gila.storeapp.product.importing.CsvImportReport;
import com.gila.storeapp.product.importing.ImportJobResponse;
import com.gila.storeapp.product.importing.ImportJobService;
import com.gila.storeapp.product.importing.ProductCsvImporter;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService productService;
    private final ProductCsvImporter productCsvImporter;
    private final ImportJobService importJobService;

    public ProductController(
        ProductService productService,
        ProductCsvImporter productCsvImporter,
        ImportJobService importJobService
    ) {
        this.productService = productService;
        this.productCsvImporter = productCsvImporter;
        this.importJobService = importJobService;
    }

    @GetMapping
    public List<ProductResponse> search(
        @RequestParam(required = false) String query,
        @RequestParam(required = false) String category
    ) {
        return productService.search(query, category);
    }

    @GetMapping("/categories")
    public List<String> categories() {
        return productService.categories();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse create(@Valid @RequestBody ProductRequest request) {
        return productService.create(request);
    }

    @PutMapping("/{id}")
    public ProductResponse update(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        return productService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        productService.delete(id);
    }

    @PostMapping("/import")
    public CsvImportReport importProducts(@RequestParam("file") MultipartFile file) throws IOException {
        return productCsvImporter.importCsv(file.getInputStream());
    }

    @PostMapping("/import-jobs")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ImportJobResponse importProductsAsync(@RequestParam("file") MultipartFile file) throws IOException {
        return importJobService.createJob(file.getOriginalFilename(), file.getBytes());
    }

    @GetMapping("/import-jobs/{id}")
    public ImportJobResponse importJob(@PathVariable Long id) {
        return importJobService.getJob(id);
    }

    @GetMapping("/import-jobs")
    public List<ImportJobResponse> importJobs() {
        return importJobService.listJobs();
    }
}
