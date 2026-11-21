package com.wordwang.highscore;

import com.wordwang.daily.DailyChallengeRepository;
import com.wordwang.leaderboard.Leaderboard;
import com.wordwang.leaderboard.LeaderboardCandidate;
import com.wordwang.leaderboard.LeaderboardPageView;
import com.wordwang.leaderboard.LeaderboardQuery;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.OptionalInt;
import java.util.stream.Stream;

/**
 * The main ("all-time") leaderboard. It shows every normal game's score together with every Daily
 * Wang score (flagged {@code daily}); the daily scores live in their own table and are merged in
 * when the board is read, so the separate daily leaderboards are unaffected.
 */
@Service
public class HighScoreService {

    private final HighScoreRepository repository;
    private final DailyChallengeRepository dailyRepository;

    public HighScoreService(HighScoreRepository repository, DailyChallengeRepository dailyRepository) {
        this.repository = repository;
        this.dailyRepository = dailyRepository;
    }

    public void recordScore(String playerName, int score, String gameId, Integer maxPossibleScore) {
        repository.save(new HighScoreEntry(playerName, score, Instant.now(), gameId, maxPossibleScore));
    }

    public LeaderboardPageView getLeaderboard(LeaderboardQuery query) {
        return Leaderboard.page(allCandidates(repository.findAll()), query);
    }

    /**
     * Where the score {@code playerName} recorded in {@code gameId} sits on the main board (every
     * score counts, repeat players and daily scores included), or empty if no such score was recorded.
     * Daily games have no game id on their entries, so this only finds normal games.
     */
    public OptionalInt rankOf(String gameId, String playerName) {
        List<HighScoreEntry> entries = repository.findAll();
        return entries.stream()
                .filter(e -> gameId.equals(e.getGameId()) && e.getPlayerName().equalsIgnoreCase(playerName))
                .findFirst()
                .map(e -> OptionalInt.of(Leaderboard.rankOf(
                        allCandidates(entries),
                        LeaderboardCandidate.of(
                                e.getPlayerName(), e.getScore(), e.getMaxPossibleScore(), e.getPlayedAt()))))
                .orElse(OptionalInt.empty());
    }

    private List<LeaderboardCandidate> allCandidates(List<HighScoreEntry> highScores) {
        Stream<LeaderboardCandidate> normal = highScores.stream()
                .map(e -> LeaderboardCandidate.of(
                        e.getPlayerName(), e.getScore(), e.getMaxPossibleScore(), e.getPlayedAt()));
        Stream<LeaderboardCandidate> daily = dailyRepository.findAll().stream()
                .map(e -> LeaderboardCandidate.of(
                        e.getPlayerName(), e.getScore(), e.getMaxPossibleScore(), e.getCompletedAt(), true));
        return Stream.concat(normal, daily).toList();
    }

    /** Clears the normal-game scores only; the Daily Wang history is separate and is kept. */
    public void clearAll() {
        repository.deleteAll();
    }
}
