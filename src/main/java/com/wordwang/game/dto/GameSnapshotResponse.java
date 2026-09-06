package com.wordwang.game.dto;

import com.wordwang.game.model.GameStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record GameSnapshotResponse(
        String gameId,
        GameStatus status,
        UUID organiserId,
        String scrambledWord,
        String solutionWord,
        Instant countdownEndsAt,
        Instant endsAt,
        List<PlayerView> players,
        List<String> yourFoundWords,
        List<PlayerView> winners) {
}
