package it.aredegalli.dominatus.service.audit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.aredegalli.dominatus.enums.AuditEventTypeEnum;
import it.aredegalli.dominatus.model.AuditEventType;
import it.aredegalli.dominatus.model.AuditLog;
import it.aredegalli.dominatus.model.User;
import it.aredegalli.dominatus.repository.AuditEventTypeRepository;
import it.aredegalli.dominatus.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {
    private final AuditLogRepository auditLogRepository;
    private final AuditEventTypeRepository eventTypeRepository;
    private final ObjectMapper objectMapper;

    @Async
    @Override
    public void logEvent(User user, AuditEventTypeEnum eventEnum, String appName, String description, Object metadata) {
        AuditEventType eventType = eventTypeRepository.findByDescription(eventEnum.name())
                .orElseThrow(() -> new IllegalArgumentException("Invalid event type: " + eventEnum.name()));

        AuditLog audit = null;
        String metadataJson = null;
        try {
            metadataJson = objectMapper.writeValueAsString(metadata);

            audit = AuditLog.builder()
                    .user(user)
                    .eventType(eventType)
                    .applicationName(appName)
                    .description(description)
                    .metadata(metadataJson)
                    .timestamp(Instant.now())
                    .build();

            log.info("[AUDIT] Audit event: {}", audit);
        } catch (JsonProcessingException e) {
            audit = AuditLog.builder()
                    .user(user)
                    .eventType(eventTypeRepository.findByDescription(AuditEventTypeEnum.AUDIT_ERROR.name()).orElseThrow())
                    .applicationName(appName)
                    .description(description)
                    .metadata("Error serializing metadata: " + e.getMessage())
                    .timestamp(Instant.now())
                    .build();

            log.warn("[AUDIT] Error serializing metadata: {}", metadata, e);
        }

        auditLogRepository.save(audit);
    }
}
