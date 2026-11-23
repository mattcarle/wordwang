package com.wordwang.game.dto;

import com.wordwang.game.model.GameStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record GameSnapshotResponse(
        String gameId,
        String name,
        boolean singlePlayer,
        GameStatus status,
        UUID organiserId,
        String scrambledWord,
        String solutionWord,
        Instant countdownEndsAt,
        Instant endsAt,
        List<PlayerView> players,
        List<String> yourFoundWords,
        List<PlayerView> winners,
        int maxPossibleScore,
        LocalDate dailyChallengeDate,
        /** The server's clock when the snapshot was taken, so clients can correct for their own clock being off. */
        Instant serverTime) {
}
