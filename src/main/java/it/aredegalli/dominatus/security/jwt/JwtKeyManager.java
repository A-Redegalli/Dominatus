package it.aredegalli.dominatus.security.jwt;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import it.aredegalli.dominatus.enums.AuditEventTypeEnum;
import it.aredegalli.dominatus.service.audit.AuditService;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class JwtKeyManager {

    private final Map<String, SecretKey> keys = new ConcurrentHashMap<>();
    private final AuditService auditService;

    public JwtKeyManager(AuditService auditService) {
        this.auditService = auditService;
    }

    @Getter
    private volatile String activeKeyId;

    @PostConstruct
    public void init() {
        rotateKey();
    }

    public synchronized void rotateKey() {
        this.rotateKey(false);
    }

    @Deprecated
    public synchronized void rotateKey(boolean scheduler) {
        String newKid = UUID.randomUUID().toString();
        SecretKey newKey = Keys.secretKeyFor(SignatureAlgorithm.HS512);
        keys.put(newKid, newKey);
        activeKeyId = newKid;
        auditService.logEvent(null, AuditEventTypeEnum.KEY_ROTATION, "Dominatus", "Key Rotation", Map.of(
                "event", AuditEventTypeEnum.KEY_ROTATION.name(),
                "timestamp", Instant.now(),
                "scheduler", scheduler));
    }

    public SecretKey getActiveKey() {
        return keys.get(activeKeyId);
    }

    public SecretKey getKeyById(String kid) {
        return keys.get(kid);
    }
}
