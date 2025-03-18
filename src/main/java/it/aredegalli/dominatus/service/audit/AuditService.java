package it.aredegalli.dominatus.service.audit;

import it.aredegalli.dominatus.enums.AuditEventTypeEnum;
import it.aredegalli.dominatus.model.User;

public interface AuditService {
    void logEvent(User user, AuditEventTypeEnum eventEnum, String appName, String description, Object metadata);
}
