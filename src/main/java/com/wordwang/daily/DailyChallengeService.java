package com.wordwang.daily;

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

    public List<DailyChallengeView> getLeaderboard(LocalDate date, int limit) {
        return repository.findByChallengeDateOrderByScoreDesc(date, PageRequest.of(0, limit)).stream()
                .map(DailyChallengeView::from)
                .toList();
    }

    public List<DailyHistoryEntryView> getHistory(int limit) {
        return repository.findPastChallengeDays(today(), PageRequest.of(0, limit)).stream()
                .map(row -> new DailyHistoryEntryView(row.getChallengeDate(), row.getSolutionWord()))
                .toList();
    }
}
