package com.wordwang.highscore;

import java.time.Instant;

public record HighScoreView(String playerName, int score, Instant playedAt, Integer percentOfMaxPossible) {

    public static HighScoreView from(HighScoreEntry entry) {
        Integer maxPossibleScore = entry.getMaxPossibleScore();
        Integer percent = maxPossibleScore == null || maxPossibleScore <= 0
                ? null
                : Math.round(entry.getScore() * 100f / maxPossibleScore);
        return new HighScoreView(entry.getPlayerName(), entry.getScore(), entry.getPlayedAt(), percent);
    }
}
