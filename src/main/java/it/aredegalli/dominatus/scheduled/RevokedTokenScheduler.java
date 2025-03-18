package it.aredegalli.dominatus.scheduled;

import it.aredegalli.dominatus.repository.RevokedTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class RevokedTokenScheduler {

    private final RevokedTokenRepository revokedTokenRepository;

    @Scheduled(cron = "0 0 * * * *")
    public void cleanRevokedTokens() {
        revokedTokenRepository.deleteExpiredTokens(Instant.now());
        log.info("[RevokedTokens - Scheduler] RevokedToken cleanup executed at {}", Instant.now());
    }

}
