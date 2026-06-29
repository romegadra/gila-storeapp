package com.gila.storeapp.product;

import com.gila.storeapp.shared.NotFoundException;
import com.gila.storeapp.shared.TextSafety;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final TextSafety textSafety;

    public ProductService(ProductRepository productRepository, TextSafety textSafety) {
        this.productRepository = productRepository;
        this.textSafety = textSafety;
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> search(String query, String category) {
        return productRepository.search(textSafety.clean(query), textSafety.clean(category)).stream()
            .map(ProductResponse::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> search(String query, String category, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        PageRequest pageRequest = PageRequest.of(safePage, safeSize, Sort.by("name").ascending());
        return productRepository.searchPage(textSafety.clean(query), textSafety.clean(category), pageRequest)
            .map(ProductResponse::from);
    }

    @Transactional(readOnly = true)
    public List<String> categories() {
        return productRepository.findCategories();
    }

    @Transactional(readOnly = true)
    public Product getProduct(Long id) {
        return productRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Product not found"));
    }

    @Transactional(readOnly = true)
    public Product getProductForUpdate(Long id) {
        return productRepository.findByIdForUpdate(id)
            .orElseThrow(() -> new NotFoundException("Product not found"));
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        productRepository.findBySkuIgnoreCase(request.sku().trim()).ifPresent(product -> {
            throw new IllegalArgumentException("SKU already exists");
        });
        Product product = new Product();
        apply(product, request);
        Product saved = productRepository.save(product);
        return ProductResponse.from(saved);
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = getProduct(id);
        if (productRepository.existsBySkuIgnoreCaseAndIdNot(request.sku().trim(), id)) {
            throw new IllegalArgumentException("SKU already exists");
        }
        apply(product, request);
        product.setUpdatedAt(Instant.now());
        return ProductResponse.from(productRepository.save(product));
    }

    @Transactional
    public void delete(Long id) {
        Product product = getProduct(id);
        productRepository.delete(product);
    }

    private void apply(Product product, ProductRequest request) {
        product.setName(textSafety.requireSafe("name", request.name()));
        product.setSku(textSafety.requireSafe("sku", request.sku()));
        product.setDescription(textSafety.requireSafe("description", request.description()));
        product.setCategory(textSafety.requireSafe("category", request.category()));
        product.setPrice(request.price());
        product.setStock(request.stock());
        product.setWeightKg(request.weightKg());
    }
}
