package com.gila.storeapp.product.importing;

import com.gila.storeapp.shared.NotFoundException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ImportJobService {
    private final ImportJobRepository importJobRepository;
    private final ImportJobWorker importJobWorker;

    public ImportJobService(ImportJobRepository importJobRepository, ImportJobWorker importJobWorker) {
        this.importJobRepository = importJobRepository;
        this.importJobWorker = importJobWorker;
    }

    public ImportJobResponse createJob(String fileName, byte[] content, String idempotencyKey) {
        String cleanKey = clean(idempotencyKey);
        if (cleanKey != null) {
            var existing = importJobRepository.findByIdempotencyKey(cleanKey);
            if (existing.isPresent()) {
                return ImportJobResponse.from(existing.get());
            }
        }
        ImportJob job = new ImportJob();
        job.setFileName(fileName);
        job.setIdempotencyKey(cleanKey);
        ImportJob saved = importJobRepository.save(job);
        importJobWorker.process(saved.getId(), content);
        return ImportJobResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public ImportJobResponse getJob(Long id) {
        return importJobRepository.findById(id)
            .map(ImportJobResponse::from)
            .orElseThrow(() -> new NotFoundException("Import job not found"));
    }

    @Transactional(readOnly = true)
    public List<ImportJobResponse> listJobs() {
        return importJobRepository.findAll().stream()
            .map(ImportJobResponse::from)
            .toList();
    }

    private String clean(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
