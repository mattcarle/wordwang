package com.wordwang.game.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record GameEndResult(
        String gameId,
        Instant createdAt,
        String solutionWord,
        List<PlayerView> winners,
        List<PlayerView> players,
        List<PlayerAuditView> playerAudits,
        int maxPossibleScore,
        LocalDate dailyChallengeDate,
        UUID dailyPlayerId,
        Instant startedAt,
        Instant finishedAt,
        boolean endedByQuit) {
}
