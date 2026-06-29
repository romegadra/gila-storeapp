package com.gila.storeapp.purchase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.gila.storeapp.product.ProductRequest;
import com.gila.storeapp.product.ProductService;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@TestPropertySource(properties = "app.csv.seed-path=missing.csv")
class PurchaseServiceTest {
    @Autowired
    private ProductService productService;

    @Autowired
    private PurchaseService purchaseService;

    @Test
    void purchaseDecrementsStockAndReturnsPaidPurchase() {
        Long productId = productService.create(new ProductRequest(
            "Desk Lamp",
            "LAMP-1",
            "Adjustable lamp",
            "Home",
            new BigDecimal("29.99"),
            7,
            new BigDecimal("0.80")
        )).id();

        PurchaseResponse response = purchaseService.purchase(new PurchaseRequest(List.of(
            new PurchaseRequest.PurchaseLineRequest(productId, 2)
        )));

        assertThat(response.status()).isEqualTo("PAID");
        assertThat(response.total()).isEqualByComparingTo("59.98");
        assertThat(productService.getProduct(productId).getStock()).isEqualTo(5);
    }

    @Test
    void purchaseFailsWhenStockIsInsufficient() {
        Long productId = productService.create(new ProductRequest(
            "Notebook",
            "NOTE-1",
            "Hardcover notebook",
            "Office",
            new BigDecimal("9.99"),
            1,
            new BigDecimal("0.30")
        )).id();

        assertThatThrownBy(() -> purchaseService.purchase(new PurchaseRequest(List.of(
            new PurchaseRequest.PurchaseLineRequest(productId, 2)
        )))).isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Not enough stock");

        assertThat(productService.getProduct(productId).getStock()).isEqualTo(1);
    }
}
