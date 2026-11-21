package com.wordwang.leaderboard;

import java.time.Instant;

/**
 * One recorded score, before it has been ranked against the others on its leaderboard.
 * {@code daily} marks a score from a Daily Wang game (the all-time board mixes both kinds).
 */
public record LeaderboardCandidate(String playerName, int score, Integer percentOfMaxPossible, Instant date,
                                   boolean daily) {

    /** A normal (non-daily) score. See {@link #of(String, int, Integer, Instant, boolean)}. */
    public static LeaderboardCandidate of(String playerName, int score, Integer maxPossibleScore, Instant date) {
        return of(playerName, score, maxPossibleScore, date, false);
    }

    /** {@code maxPossibleScore} is nullable: high-score entries recorded before it existed have none. */
    public static LeaderboardCandidate of(String playerName, int score, Integer maxPossibleScore, Instant date,
                                          boolean daily) {
        Integer percent = maxPossibleScore == null || maxPossibleScore <= 0
                ? null
                : Math.round(score * 100f / maxPossibleScore);
        return new LeaderboardCandidate(playerName, score, percent, date, daily);
    }
}
