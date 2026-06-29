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

    public ImportJobResponse createJob(String fileName, byte[] content) {
        ImportJob job = new ImportJob();
        job.setFileName(fileName);
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

}
