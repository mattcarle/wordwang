package com.wordwang.leaderboard;

import java.time.Instant;

/**
 * {@code gameCount} is the player's total games on this board; null unless grouped by player.
 * {@code daily} is true when the score came from a Daily Wang game.
 */
public record LeaderboardEntryView(int rank, String playerName, int score, Integer percentOfMaxPossible,
                                   Instant date, Integer gameCount, boolean daily) {
}
