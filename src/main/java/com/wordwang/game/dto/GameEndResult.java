package com.wordwang.game.dto;

import java.time.Instant;
import java.util.List;

public record GameEndResult(
        String gameId,
        Instant createdAt,
        String solutionWord,
        List<PlayerView> winners,
        List<PlayerView> players,
        List<PlayerAuditView> playerAudits) {
}
