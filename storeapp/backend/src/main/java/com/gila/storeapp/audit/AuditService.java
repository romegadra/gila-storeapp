package com.gila.storeapp.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AuditService {
    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    private final AuditEventRepository auditEventRepository;

    public AuditService(AuditEventRepository auditEventRepository) {
        this.auditEventRepository = auditEventRepository;
    }

    public void record(String eventType, String aggregateType, String aggregateId, String details) {
        AuditEvent event = new AuditEvent();
        event.setEventType(eventType);
        event.setAggregateType(aggregateType);
        event.setAggregateId(aggregateId);
        event.setDetails(details);
        auditEventRepository.save(event);
        log.info("audit eventType={} aggregateType={} aggregateId={} details={}", eventType, aggregateType, aggregateId, details);
    }
}
