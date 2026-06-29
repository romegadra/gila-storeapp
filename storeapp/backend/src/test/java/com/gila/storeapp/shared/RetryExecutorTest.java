package com.gila.storeapp.shared;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.dao.TransientDataAccessResourceException;

class RetryExecutorTest {
    private final RetryExecutor retryExecutor = new RetryExecutor();

    @Test
    void retriesTransientFailures() {
        AtomicInteger attempts = new AtomicInteger();

        String result = retryExecutor.execute(() -> {
            if (attempts.incrementAndGet() < 3) {
                throw new TransientDataAccessResourceException("temporary outage");
            }
            return "saved";
        });

        assertThat(result).isEqualTo("saved");
        assertThat(attempts).hasValue(3);
    }
}
