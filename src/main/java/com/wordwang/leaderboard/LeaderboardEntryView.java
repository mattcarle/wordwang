package com.wordwang.leaderboard;

import java.time.Instant;

public record LeaderboardEntryView(int rank, String playerName, int score, Integer percentOfMaxPossible,
                                   Instant date) {
}
