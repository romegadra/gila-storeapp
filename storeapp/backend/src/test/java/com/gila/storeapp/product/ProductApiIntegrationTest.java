package com.gila.storeapp.product;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestPropertySource(properties = "app.csv.seed-path=missing.csv")
class ProductApiIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void productCrudAndSearchWorkThroughHttp() throws Exception {
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "Trail Backpack",
                      "sku": "PACK-1",
                      "description": "Weather resistant",
                      "category": "Outdoors",
                      "price": 79.99,
                      "stock": 12,
                      "weightKg": 0.9
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.sku").value("PACK-1"));

        mockMvc.perform(get("/api/products").param("query", "trail"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("Trail Backpack"));
    }

    @Test
    void importEndpointReturnsRowLevelFailures() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "products.csv",
            "text/csv",
            """
                name,sku,description,category,price,stock,weight_kg
                Valid,VALID-1,Good,General,11.00,2,0.4
                Invalid,INVALID-1,Bad,General,free,2,0.4
                """.getBytes()
        );

        mockMvc.perform(multipart("/api/products/import").file(file))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.created").value(1))
            .andExpect(jsonPath("$.skipped").value(1))
            .andExpect(jsonPath("$.errors[0].message", containsString("price")));
    }

    @Test
    void rejectsUnsafeProductText() throws Exception {
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": "<script>alert('xss')</script>",
                      "sku": "XSS-1",
                      "description": "Unsafe name",
                      "category": "Security",
                      "price": 10.00,
                      "stock": 1,
                      "weightKg": 0.1
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message", containsString("unsafe markup")));
    }
}
