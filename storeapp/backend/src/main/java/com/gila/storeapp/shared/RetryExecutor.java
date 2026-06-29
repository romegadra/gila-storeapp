package com.gila.storeapp.shared;

import java.time.Duration;
import java.util.function.Supplier;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.stereotype.Component;

@Component
public class RetryExecutor {
    public <T> T execute(Supplier<T> operation) {
        RuntimeException last = null;
        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                return operation.get();
            } catch (TransientDataAccessException ex) {
                last = ex;
                sleep(attempt);
            }
        }
        throw last;
    }

    private void sleep(int attempt) {
        try {
            Thread.sleep(Duration.ofMillis(75L * attempt).toMillis());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Retry interrupted", ex);
        }
    }
}
