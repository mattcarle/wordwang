package com.wordwang.audit;

import com.wordwang.game.dto.GameEndResult;
import com.wordwang.game.dto.PlayerAuditView;
import com.wordwang.geolocation.GeolocationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    private final GameAuditRepository repository;
    private final GeolocationService geolocationService;

    public AuditService(GameAuditRepository repository, GeolocationService geolocationService) {
        this.repository = repository;
        this.geolocationService = geolocationService;
    }

    /**
     * Runs on the scheduled-finalization thread (see GameFinalizerScheduler), not a player-facing
     * request thread, so the geolocation lookups' latency never slows down gameplay.
     */
    public void recordGame(GameEndResult result) {
        long startNanos = System.nanoTime();
        GameAudit audit = new GameAudit(result.gameId(), result.createdAt(), result.solutionWord(),
                result.startedAt(), result.finishedAt(), result.maxPossibleScore(), result.dailyChallengeDate(),
                result.endedByQuit());
        long geolocationNanos = 0;
        for (PlayerAuditView player : result.playerAudits()) {
            long lookupStart = System.nanoTime();
            String location = geolocationService.locate(player.ipAddress());
            long lookupNanos = System.nanoTime() - lookupStart;
            geolocationNanos += lookupNanos;
            log.info("End-of-game timing [game {}]: geolocation lookup for {} took {}ms ({})",
                    result.gameId(), player.name(), lookupNanos / 1_000_000, location);
            audit.addPlayer(new PlayerAudit(player.name(), player.organiser(), player.score(),
                    player.foundEightLetterWord(), player.winner(), player.ipAddress(), location,
                    player.foundWords()));
        }
        long saveStart = System.nanoTime();
        repository.save(audit);
        long endNanos = System.nanoTime();
        log.info("End-of-game timing [game {}]: audit recorded in {}ms (geolocation {}ms, save {}ms, {} players)",
                result.gameId(), (endNanos - startNanos) / 1_000_000, geolocationNanos / 1_000_000,
                (endNanos - saveStart) / 1_000_000, result.playerAudits().size());
    }

    public AuditPageResponse search(Instant from, Instant to, int page, int size) {
        Instant effectiveFrom = from != null ? from : Instant.EPOCH;
        Instant effectiveTo = to != null ? to : Instant.now();
        Page<GameAudit> result = repository.findByCreatedAtBetweenOrderByCreatedAtDesc(
                effectiveFrom, effectiveTo, PageRequest.of(page, size));
        return new AuditPageResponse(
                result.getContent().stream().map(AuditGameView::from).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages());
    }
}
