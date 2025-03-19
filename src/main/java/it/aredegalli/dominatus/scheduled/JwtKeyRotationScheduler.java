package it.aredegalli.dominatus.scheduled;

import it.aredegalli.dominatus.security.jwt.JwtKeyManager;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class JwtKeyRotationScheduler {

    private final JwtKeyManager keyManager;

    @Scheduled(cron = "0 0 0 * * ?")
    public void rotateKeyDaily() {
        keyManager.rotateKey(true);
        System.out.println("[JWT] Rotated signing key at " + Instant.now());
    }
}