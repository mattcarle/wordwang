package com.wordwang.highscore;

import java.time.Instant;

public record HighScoreView(String playerName, int score, Instant playedAt) {

    public static HighScoreView from(HighScoreEntry entry) {
        return new HighScoreView(entry.getPlayerName(), entry.getScore(), entry.getPlayedAt());
    }
}
