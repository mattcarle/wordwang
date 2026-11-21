package com.wordwang.leaderboard;

import java.util.List;

public record LeaderboardPageView(List<LeaderboardEntryView> entries, boolean hasMore) {
}
