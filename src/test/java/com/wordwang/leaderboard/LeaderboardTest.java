package com.wordwang.leaderboard;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LeaderboardTest {

    private static final Instant T0 = Instant.parse("2026-10-01T12:00:00Z");

    private static LeaderboardCandidate entry(String name, int score, Integer max, int minutesAfterT0) {
        return LeaderboardCandidate.of(name, score, max, T0.plusSeconds(minutesAfterT0 * 60L));
    }

    private static LeaderboardPageView page(List<LeaderboardCandidate> candidates, String sort, String direction,
                                            boolean uniquePlayers, int offset, int limit) {
        return Leaderboard.page(candidates, LeaderboardQuery.fromParams(sort, direction, uniquePlayers, offset, limit));
    }

    private static List<String> names(LeaderboardPageView page) {
        return page.entries().stream().map(LeaderboardEntryView::playerName).toList();
    }

    private static List<Integer> ranks(LeaderboardPageView page) {
        return page.entries().stream().map(LeaderboardEntryView::rank).toList();
    }

    @Test
    void computesPercentOfMaxPossibleScore() {
        assertThat(LeaderboardCandidate.of("Alice", 25, 100, T0).percentOfMaxPossible()).isEqualTo(25);
    }

    @Test
    void percentIsNullWhenMaxPossibleScoreWasNeverRecorded() {
        assertThat(LeaderboardCandidate.of("Alice", 25, null, T0).percentOfMaxPossible()).isNull();
    }

    @Test
    void percentIsNullRatherThanDivideByZeroWhenMaxPossibleScoreIsZero() {
        assertThat(LeaderboardCandidate.of("Alice", 0, 0, T0).percentOfMaxPossible()).isNull();
    }

    @Test
    void equalScoresAreSplitByHigherPercentOfMax() {
        List<LeaderboardCandidate> candidates = List.of(
                entry("Low", 50, 200, 0),   // 25%
                entry("High", 50, 100, 1),  // 50%
                entry("Top", 80, 400, 2));  // 20%, but the highest score

        LeaderboardPageView result = page(candidates, "rank", "asc", false, 0, 20);

        assertThat(names(result)).containsExactly("Top", "High", "Low");
        assertThat(ranks(result)).containsExactly(1, 2, 3);
    }

    @Test
    void equalScoreAndEqualPercentIsATieUsingCompetitionRanking() {
        List<LeaderboardCandidate> candidates = List.of(
                entry("First", 50, 100, 0),
                entry("Second", 50, 100, 1),
                entry("Third", 40, 100, 2));

        assertThat(ranks(page(candidates, "rank", "asc", false, 0, 20))).containsExactly(1, 1, 3);
    }

    @Test
    void entriesWithNoRecordedPercentRankBelowEqualScoresThatHaveOne() {
        List<LeaderboardCandidate> candidates = List.of(entry("Legacy", 50, null, 0), entry("Modern", 50, 1000, 1));

        LeaderboardPageView result = page(candidates, "rank", "asc", false, 0, 20);

        assertThat(names(result)).containsExactly("Modern", "Legacy");
        assertThat(ranks(result)).containsExactly(1, 2);
    }

    @Test
    void uniquePlayersKeepsOnlyEachPlayersBestEntryAndReRanks() {
        List<LeaderboardCandidate> candidates = List.of(
                entry("Alice", 90, 100, 0),
                entry("alice ", 80, 100, 1),
                entry("Bob", 85, 100, 2),
                entry("Alice", 70, 100, 3));

        assertThat(names(page(candidates, "rank", "asc", false, 0, 20)))
                .containsExactly("Alice", "Bob", "alice ", "Alice");

        LeaderboardPageView unique = page(candidates, "rank", "asc", true, 0, 20);
        assertThat(names(unique)).containsExactly("Alice", "Bob");
        assertThat(ranks(unique)).containsExactly(1, 2);
        assertThat(unique.entries().get(0).score()).isEqualTo(90);
    }

    @Test
    void sortsByEachColumnInBothDirectionsKeepingTheRealRank() {
        List<LeaderboardCandidate> candidates = List.of(
                entry("carol", 30, 100, 5),
                entry("Alice", 90, 100, 1),
                entry("Bob", 60, 100, 9));

        assertThat(names(page(candidates, "score", "asc", false, 0, 20))).containsExactly("carol", "Bob", "Alice");
        assertThat(names(page(candidates, "score", "desc", false, 0, 20))).containsExactly("Alice", "Bob", "carol");
        assertThat(names(page(candidates, "player", "asc", false, 0, 20))).containsExactly("Alice", "Bob", "carol");
        assertThat(names(page(candidates, "player", "desc", false, 0, 20))).containsExactly("carol", "Bob", "Alice");
        assertThat(names(page(candidates, "date", "desc", false, 0, 20))).containsExactly("Bob", "carol", "Alice");
        assertThat(names(page(candidates, "rank", "desc", false, 0, 20))).containsExactly("carol", "Bob", "Alice");

        assertThat(ranks(page(candidates, "score", "asc", false, 0, 20))).containsExactly(3, 2, 1);
    }

    @Test
    void sortingByPercentKeepsEntriesWithNoPercentLastInBothDirections() {
        List<LeaderboardCandidate> candidates = List.of(
                entry("Legacy", 99, null, 0),
                entry("Half", 50, 100, 1),
                entry("Tenth", 10, 100, 2));

        assertThat(names(page(candidates, "percent", "desc", false, 0, 20))).containsExactly("Half", "Tenth", "Legacy");
        assertThat(names(page(candidates, "percent", "asc", false, 0, 20))).containsExactly("Tenth", "Half", "Legacy");
    }

    @Test
    void pagesThroughTheSortedResultAndReportsWhetherMoreRemain() {
        List<LeaderboardCandidate> candidates = List.of(
                entry("A", 50, 100, 0), entry("B", 40, 100, 1), entry("C", 30, 100, 2),
                entry("D", 20, 100, 3), entry("E", 10, 100, 4));

        LeaderboardPageView first = page(candidates, "rank", "asc", false, 0, 2);
        LeaderboardPageView last = page(candidates, "rank", "asc", false, 4, 2);
        LeaderboardPageView beyond = page(candidates, "rank", "asc", false, 10, 2);

        assertThat(names(first)).containsExactly("A", "B");
        assertThat(first.hasMore()).isTrue();
        assertThat(names(last)).containsExactly("E");
        assertThat(ranks(last)).containsExactly(5);
        assertThat(last.hasMore()).isFalse();
        assertThat(beyond.entries()).isEmpty();
        assertThat(beyond.hasMore()).isFalse();
    }

    @Test
    void rejectsUnknownSortsDirectionsAndOutOfRangePaging() {
        assertThatThrownBy(() -> LeaderboardQuery.fromParams("nope", "asc", false, 0, 20)).hasMessageContaining("sort");
        assertThatThrownBy(() -> LeaderboardQuery.fromParams("rank", "up", false, 0, 20)).hasMessageContaining("direction");
        assertThatThrownBy(() -> LeaderboardQuery.fromParams("rank", "asc", false, -1, 20)).hasMessageContaining("offset");
        assertThatThrownBy(() -> LeaderboardQuery.fromParams("rank", "asc", false, 0, 0)).hasMessageContaining("limit");
        assertThatThrownBy(() -> LeaderboardQuery.fromParams("rank", "asc", false, 0, 101)).hasMessageContaining("limit");
    }
}
