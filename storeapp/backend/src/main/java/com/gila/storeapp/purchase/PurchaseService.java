package com.gila.storeapp.purchase;

import com.gila.storeapp.product.Product;
import com.gila.storeapp.product.ProductService;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PurchaseService {
    private final ProductService productService;
    private final PurchaseRepository purchaseRepository;

    public PurchaseService(ProductService productService, PurchaseRepository purchaseRepository) {
        this.productService = productService;
        this.purchaseRepository = purchaseRepository;
    }

    @Transactional
    public PurchaseResponse purchase(PurchaseRequest request) {
        Purchase purchase = new Purchase();
        purchase.setStatus("PAID");
        BigDecimal total = BigDecimal.ZERO;

        for (PurchaseRequest.PurchaseLineRequest line : request.items()) {
            Product product = productService.getProduct(line.productId());
            if (product.getStock() < line.quantity()) {
                throw new IllegalArgumentException("Not enough stock for SKU " + product.getSku());
            }

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
        }

        purchase.setTotal(total);
        return PurchaseResponse.from(purchaseRepository.save(purchase));
    }
}
