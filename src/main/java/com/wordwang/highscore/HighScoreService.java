package com.wordwang.highscore;

import com.wordwang.leaderboard.Leaderboard;
import com.wordwang.leaderboard.LeaderboardCandidate;
import com.wordwang.leaderboard.LeaderboardPageView;
import com.wordwang.leaderboard.LeaderboardQuery;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.OptionalInt;

@Service
public class HighScoreService {

    private final HighScoreRepository repository;

    public HighScoreService(HighScoreRepository repository) {
        this.repository = repository;
    }

    public void recordScore(String playerName, int score, String gameId, Integer maxPossibleScore) {
        repository.save(new HighScoreEntry(playerName, score, Instant.now(), gameId, maxPossibleScore));
    }

    public LeaderboardPageView getLeaderboard(LeaderboardQuery query) {
        List<LeaderboardCandidate> candidates = repository.findAll().stream()
                .map(e -> LeaderboardCandidate.of(e.getPlayerName(), e.getScore(), e.getMaxPossibleScore(),
                        e.getPlayedAt()))
                .toList();
        return Leaderboard.page(candidates, query);
    }

    /**
     * Where the score {@code playerName} recorded in {@code gameId} sits on the all-time board (every
     * score counts, repeat players included), or empty if no such score was recorded.
     */
    public OptionalInt rankOf(String gameId, String playerName) {
        List<HighScoreEntry> entries = repository.findAll();
        return entries.stream()
                .filter(e -> gameId.equals(e.getGameId()) && e.getPlayerName().equalsIgnoreCase(playerName))
                .findFirst()
                .map(e -> OptionalInt.of(Leaderboard.rankOf(
                        entries.stream()
                                .map(x -> LeaderboardCandidate.of(
                                        x.getPlayerName(), x.getScore(), x.getMaxPossibleScore(), x.getPlayedAt()))
                                .toList(),
                        LeaderboardCandidate.of(
                                e.getPlayerName(), e.getScore(), e.getMaxPossibleScore(), e.getPlayedAt()))))
                .orElse(OptionalInt.empty());
    }

    public void clearAll() {
        repository.deleteAll();
    }
}
