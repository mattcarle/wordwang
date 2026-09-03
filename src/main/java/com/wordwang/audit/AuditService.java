package com.wordwang.audit;

import com.wordwang.game.dto.GameEndResult;
import com.wordwang.game.dto.PlayerAuditView;
import com.wordwang.geolocation.GeolocationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class AuditService {

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
        GameAudit audit = new GameAudit(result.gameId(), result.createdAt(), result.solutionWord());
        for (PlayerAuditView player : result.playerAudits()) {
            String location = geolocationService.locate(player.ipAddress());
            audit.addPlayer(new PlayerAudit(player.name(), player.organiser(), player.score(),
                    player.foundEightLetterWord(), player.winner(), player.ipAddress(), location));
        }
        repository.save(audit);
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
