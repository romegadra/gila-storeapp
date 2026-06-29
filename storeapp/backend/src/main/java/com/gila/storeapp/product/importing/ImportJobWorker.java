package com.gila.storeapp.product.importing;

import com.gila.storeapp.shared.NotFoundException;
import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ImportJobWorker {
    private final ImportJobRepository importJobRepository;
    private final ProductCsvImporter productCsvImporter;

    public ImportJobWorker(ImportJobRepository importJobRepository, ProductCsvImporter productCsvImporter) {
        this.importJobRepository = importJobRepository;
        this.productCsvImporter = productCsvImporter;
    }

    @Async("importExecutor")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public CompletableFuture<Void> process(Long jobId, byte[] content) {
        markRunning(jobId);
        try {
            CsvImportReport report = productCsvImporter.importCsv(new ByteArrayInputStream(content));
            markCompleted(jobId, report);
        } catch (Exception ex) {
            markFailed(jobId, ex.getMessage());
        }
        return CompletableFuture.completedFuture(null);
    }

    @Transactional
    public void markRunning(Long jobId) {
        ImportJob job = getEntity(jobId);
        job.setStatus(ImportJobStatus.RUNNING);
        job.setStartedAt(Instant.now());
        importJobRepository.save(job);
    }

    @Transactional
    public void markCompleted(Long jobId, CsvImportReport report) {
        ImportJob job = getEntity(jobId);
        job.setStatus(ImportJobStatus.COMPLETED);
        job.setCreatedCount(report.created());
        job.setUpdatedCount(report.updated());
        job.setSkippedCount(report.skipped());
        job.setErrorSummary(summary(report));
        job.setFinishedAt(Instant.now());
        importJobRepository.save(job);
    }

    @Transactional
    public void markFailed(Long jobId, String message) {
        ImportJob job = getEntity(jobId);
        job.setStatus(ImportJobStatus.FAILED);
        job.setErrorSummary(message);
        job.setFinishedAt(Instant.now());
        importJobRepository.save(job);
    }

    private ImportJob getEntity(Long jobId) {
        return importJobRepository.findById(jobId)
            .orElseThrow(() -> new NotFoundException("Import job not found"));
    }

    private String summary(CsvImportReport report) {
        if (report.errors().isEmpty()) {
            return null;
        }
        return report.errors().stream()
            .limit(10)
            .map(error -> "row " + error.row() + " " + error.sku() + ": " + error.message())
            .reduce((left, right) -> left + "; " + right)
            .orElse(null);
    }
}
