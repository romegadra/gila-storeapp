package com.gila.storeapp.purchase;

import com.gila.storeapp.audit.AuditService;
import com.gila.storeapp.product.Product;
import com.gila.storeapp.product.ProductService;
import java.math.BigDecimal;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PurchaseService {
    private static final Logger log = LoggerFactory.getLogger(PurchaseService.class);

    private final ProductService productService;
    private final PurchaseRepository purchaseRepository;
    private final AuditService auditService;

    public PurchaseService(ProductService productService, PurchaseRepository purchaseRepository, AuditService auditService) {
        this.productService = productService;
        this.purchaseRepository = purchaseRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<PurchaseResponse> listPurchases() {
        return purchaseRepository.findAll(Sort.by(Sort.Direction.DESC, "purchasedAt")).stream()
            .map(PurchaseResponse::from)
            .toList();
    }

    @Transactional
    public PurchaseResponse purchase(PurchaseRequest request) {
        String idempotencyKey = clean(request.idempotencyKey());
        if (idempotencyKey != null) {
            var existing = purchaseRepository.findByIdempotencyKey(idempotencyKey);
            if (existing.isPresent()) {
                log.info("purchase.idempotent_replay idempotencyKey={} purchaseId={}", idempotencyKey, existing.get().getId());
                return PurchaseResponse.from(existing.get());
            }
        }

        Purchase purchase = new Purchase();
        purchase.setStatus("PAID");
        purchase.setIdempotencyKey(idempotencyKey);
        BigDecimal total = BigDecimal.ZERO;

        for (PurchaseRequest.PurchaseLineRequest line : request.items()) {
            Product product = productService.getProductForUpdate(line.productId());
            if (product.getStock() < line.quantity()) {
                throw new IllegalArgumentException("Not enough stock for SKU " + product.getSku());
            }

            int previousStock = product.getStock();
            BigDecimal lineTotal = product.getPrice().multiply(BigDecimal.valueOf(line.quantity()));
            product.setStock(product.getStock() - line.quantity());

            PurchaseItem item = new PurchaseItem();
            item.setProductId(product.getId());
            item.setProductName(product.getName());
            item.setSku(product.getSku());
            item.setQuantity(line.quantity());
            item.setUnitPrice(product.getPrice());
            item.setLineTotal(lineTotal);
            purchase.addItem(item);

            total = total.add(lineTotal);
            auditService.record(
                "INVENTORY_DECREMENTED",
                "Product",
                String.valueOf(product.getId()),
                "sku=" + product.getSku() + ", previousStock=" + previousStock + ", newStock=" + product.getStock()
            );
        }

        purchase.setTotal(total);
        Purchase saved = purchaseRepository.save(purchase);
        auditService.record("PURCHASE_COMPLETED", "Purchase", String.valueOf(saved.getId()), "total=" + total);
        log.info("purchase.completed purchaseId={} total={} itemCount={}", saved.getId(), total, saved.getItems().size());
        return PurchaseResponse.from(saved);
    }

    private String clean(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
