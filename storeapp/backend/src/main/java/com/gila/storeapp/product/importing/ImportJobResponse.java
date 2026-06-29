package com.gila.storeapp.product.importing;

import java.time.Instant;

public record ImportJobResponse(
    Long id,
    String fileName,
    ImportJobStatus status,
    int created,
    int updated,
    int skipped,
    String errorSummary,
    Instant createdAt,
    Instant startedAt,
    Instant finishedAt
) {
    static ImportJobResponse from(ImportJob job) {
        return new ImportJobResponse(
            job.getId(),
            job.getFileName(),
            job.getStatus(),
            job.getCreatedCount(),
            job.getUpdatedCount(),
            job.getSkippedCount(),
            job.getErrorSummary(),
            job.getCreatedAt(),
            job.getStartedAt(),
            job.getFinishedAt()
        );
    }
}
