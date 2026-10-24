package com.wordwang.daily;

import com.wordwang.leaderboard.Leaderboard;
import com.wordwang.leaderboard.LeaderboardCandidate;
import com.wordwang.leaderboard.LeaderboardPageView;
import com.wordwang.leaderboard.LeaderboardQuery;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
public class DailyChallengeService {

    private final DailyChallengeRepository repository;

    public DailyChallengeService(DailyChallengeRepository repository) {
        this.repository = repository;
    }

    public LocalDate today() {
        return LocalDate.now(ZoneOffset.UTC);
    }

    /** True once {@code dailyPlayerId} has a recorded completion for {@code challengeDate}. */
    public boolean hasCompleted(UUID dailyPlayerId, LocalDate challengeDate) {
        return dailyPlayerId != null
                && repository.findByChallengeDateAndDailyPlayerId(challengeDate, dailyPlayerId).isPresent();
    }

    public void recordCompletion(String playerName, int score, int maxPossibleScore, LocalDate challengeDate,
                                  String solutionWord, UUID dailyPlayerId) {
        repository.save(new DailyChallengeEntry(
                challengeDate, solutionWord, playerName, score, maxPossibleScore, Instant.now(), dailyPlayerId));
    }

    public LeaderboardPageView getLeaderboard(LocalDate date, LeaderboardQuery query) {
        List<LeaderboardCandidate> candidates = repository.findByChallengeDate(date).stream()
                .map(e -> LeaderboardCandidate.of(e.getPlayerName(), e.getScore(), e.getMaxPossibleScore(),
                        e.getCompletedAt()))
                .toList();
        return Leaderboard.page(candidates, query);
    }

    public List<DailyHistoryEntryView> getHistory(int limit) {
        return repository.findPastChallengeDays(today(), PageRequest.of(0, limit)).stream()
                .map(row -> new DailyHistoryEntryView(row.getChallengeDate(), row.getSolutionWord()))
                .toList();
    }
}
