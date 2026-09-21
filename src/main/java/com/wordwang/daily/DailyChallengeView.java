package com.wordwang.daily;

import java.time.Instant;

public record DailyChallengeView(String playerName, int score, Integer percentOfMaxPossible, Instant completedAt) {

    public static DailyChallengeView from(DailyChallengeEntry entry) {
        int maxPossibleScore = entry.getMaxPossibleScore();
        Integer percent = maxPossibleScore <= 0 ? null : Math.round(entry.getScore() * 100f / maxPossibleScore);
        return new DailyChallengeView(entry.getPlayerName(), entry.getScore(), percent, entry.getCompletedAt());
    }
}
