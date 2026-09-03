package com.wordwang.audit;

import java.time.Instant;
import java.util.List;

public record AuditGameView(String gameCode, Instant createdAt, String solutionWord, List<AuditPlayerView> players) {

    static AuditGameView from(GameAudit game) {
        return new AuditGameView(
                game.getGameCode(),
                game.getCreatedAt(),
                game.getSolutionWord(),
                game.getPlayers().stream().map(AuditPlayerView::from).toList());
    }
}
