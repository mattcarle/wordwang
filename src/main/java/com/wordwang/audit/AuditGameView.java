package com.wordwang.audit;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record AuditGameView(
        String gameCode,
        Instant createdAt,
        String solutionWord,
        Instant startedAt,
        Instant finishedAt,
        Integer maxPossibleScore,
        LocalDate dailyChallengeDate,
        Boolean endedByQuit,
        int playerCount,
        List<AuditPlayerView> players) {

    static AuditGameView from(GameAudit game) {
        return new AuditGameView(
                game.getGameCode(),
                game.getCreatedAt(),
                game.getSolutionWord(),
                game.getStartedAt(),
                game.getFinishedAt(),
                game.getMaxPossibleScore(),
                game.getDailyChallengeDate(),
                game.getEndedByQuit(),
                game.getPlayers().size(),
                game.getPlayers().stream().map(AuditPlayerView::from).toList());
    }
}
