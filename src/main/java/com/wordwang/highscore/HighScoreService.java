package com.wordwang.highscore;

import com.wordwang.leaderboard.Leaderboard;
import com.wordwang.leaderboard.LeaderboardCandidate;
import com.wordwang.leaderboard.LeaderboardPageView;
import com.wordwang.leaderboard.LeaderboardQuery;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

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

    public void clearAll() {
        repository.deleteAll();
    }
}
