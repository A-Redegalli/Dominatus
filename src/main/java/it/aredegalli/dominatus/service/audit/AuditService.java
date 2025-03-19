package it.aredegalli.dominatus.service.audit;

import it.aredegalli.dominatus.enums.AuditEventTypeEnum;
import it.aredegalli.dominatus.model.User;
import org.springframework.scheduling.annotation.Async;

import java.util.Map;

public interface AuditService {

    @Async
    void logEvent(User user, AuditEventTypeEnum eventEnum, String appName, String description, Map<String, Object> metadata);
}
