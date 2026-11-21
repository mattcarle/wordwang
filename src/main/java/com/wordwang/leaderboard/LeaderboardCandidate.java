package com.wordwang.leaderboard;

import java.time.Instant;

/** One recorded score, before it has been ranked against the others on its leaderboard. */
public record LeaderboardCandidate(String playerName, int score, Integer percentOfMaxPossible, Instant date) {

    /** {@code maxPossibleScore} is nullable: high-score entries recorded before it existed have none. */
    public static LeaderboardCandidate of(String playerName, int score, Integer maxPossibleScore, Instant date) {
        Integer percent = maxPossibleScore == null || maxPossibleScore <= 0
                ? null
                : Math.round(score * 100f / maxPossibleScore);
        return new LeaderboardCandidate(playerName, score, percent, date);
    }
}
