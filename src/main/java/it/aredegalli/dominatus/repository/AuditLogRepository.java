package it.aredegalli.dominatus.repository;

import it.aredegalli.dominatus.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
    List<AuditLog> findByUserId(UUID userId);

    List<AuditLog> findByApplicationName(String applicationName);
}
