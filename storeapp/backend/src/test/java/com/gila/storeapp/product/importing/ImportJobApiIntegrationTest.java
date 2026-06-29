package com.gila.storeapp.product.importing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "app.csv.seed-path=missing.csv")
class ImportJobApiIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void asyncImportJobCompletesAndStoresReport() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "products.csv",
            "text/csv",
            """
                name,sku,description,category,price,stock,weight_kg
                Async Product,ASYNC-1,Good,General,11.00,2,0.4
                """.getBytes(StandardCharsets.UTF_8)
        );

        String created = mockMvc.perform(multipart("/api/products/import-jobs").file(file))
            .andExpect(status().isAccepted())
            .andReturn()
            .getResponse()
            .getContentAsString();

        long jobId = objectMapper.readTree(created).get("id").asLong();
        JsonNode completed = waitForJob(jobId);

        assertThat(completed.get("status").asText()).isEqualTo("COMPLETED");
        assertThat(completed.get("created").asInt()).isEqualTo(1);
        assertThat(completed.get("skipped").asInt()).isZero();
    }

    private JsonNode waitForJob(long jobId) throws Exception {
        JsonNode job = null;
        for (int attempt = 0; attempt < 20; attempt++) {
            String body = mockMvc.perform(get("/api/products/import-jobs/{id}", jobId))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
            job = objectMapper.readTree(body);
            if ("COMPLETED".equals(job.get("status").asText()) || "FAILED".equals(job.get("status").asText())) {
                return job;
            }
            Thread.sleep(100);
        }
        return job;
    }
}
