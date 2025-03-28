package it.aredegalli.dominatus.service.audit;

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
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {
    private final AuditLogRepository auditLogRepository;
    private final AuditEventTypeRepository eventTypeRepository;

    @Async
    @Override
    public void logEvent(User user, AuditEventTypeEnum eventEnum, String appName, String description, Map<String, Object> metadata) {
        AuditEventType eventType = eventTypeRepository.findByDescription(eventEnum.name())
                .orElseGet(() -> eventTypeRepository.save(AuditEventType.builder().description(eventEnum.name()).build()));

        AuditLog audit = AuditLog.builder()
                .user(user)
                .eventType(eventType)
                .applicationName(appName)
                .description(description)
                .metadata(metadata)
                .timestamp(Instant.now())
                .build();

        log.info("[AUDIT] Audit event: {}", audit);

        auditLogRepository.save(audit);
    }

}
