package com.wordwang.leaderboard;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Ranks, de-duplicates, sorts and pages a leaderboard in memory. Shared by the all-time and daily
 * boards so they rank identically. Working on the full candidate list (rather than in SQL) keeps
 * ranks correct whatever column is sorted or page is requested, and is fine at this game's scale.
 */
public final class Leaderboard {

    /**
     * Higher score first; equal scores are split by higher percent of max (entries with no percent
     * last). Earliest first is only a stable display order - it doesn't break the tie for rank.
     */
    private static final Comparator<LeaderboardCandidate> RANK_ORDER = Comparator
            .comparingInt(LeaderboardCandidate::score).reversed()
            .thenComparing(LeaderboardCandidate::percentOfMaxPossible,
                    Comparator.nullsLast(Comparator.reverseOrder()))
            .thenComparing(LeaderboardCandidate::date, Comparator.nullsLast(Comparator.naturalOrder()));

    private Leaderboard() {
    }

    public static LeaderboardPageView page(List<LeaderboardCandidate> candidates, LeaderboardQuery query) {
        List<LeaderboardCandidate> ordered = candidates.stream().sorted(RANK_ORDER).toList();
        Map<String, Integer> gameCounts = null;
        if (query.uniquePlayers()) {
            // Counted before collapsing to each player's best entry, so it's their total games.
            gameCounts = ordered.stream()
                    .collect(Collectors.toMap(c -> playerKey(c.playerName()), c -> 1, Integer::sum));
            ordered = bestEntryPerPlayer(ordered);
        }

        // assignRanks returns rank order and List.sort is stable, so rank order is the tie-break
        // for whichever column is sorted.
        List<LeaderboardEntryView> sorted = assignRanks(ordered, gameCounts);
        sorted.sort(columnOrder(query.sort(), query.descending()));

        int from = Math.min(query.offset(), sorted.size());
        int to = Math.min(from + query.limit(), sorted.size());
        return new LeaderboardPageView(List.copyOf(sorted.subList(from, to)), to < sorted.size());
    }

    /**
     * The rank {@code target} holds among {@code all} (which must include it), using the same
     * competition ranking as {@link #page}: one more than the number of entries that beat it, so
     * ties share a rank. An entry beats another with a higher score, or an equal score and a higher
     * percent of max (no percent counts as lowest).
     */
    public static int rankOf(List<LeaderboardCandidate> all, LeaderboardCandidate target) {
        long better = all.stream().filter(c -> beats(c, target)).count();
        return (int) better + 1;
    }

    private static boolean beats(LeaderboardCandidate a, LeaderboardCandidate b) {
        if (a.score() != b.score()) {
            return a.score() > b.score();
        }
        Integer pa = a.percentOfMaxPossible();
        Integer pb = b.percentOfMaxPossible();
        if (pa == null) {
            return false;
        }
        return pb == null || pa > pb;
    }

    /** Players are identified by name, ignoring case and surrounding spaces - names are all there is. */
    private static List<LeaderboardCandidate> bestEntryPerPlayer(List<LeaderboardCandidate> ordered) {
        Set<String> seen = new HashSet<>();
        return ordered.stream()
                .filter(c -> seen.add(playerKey(c.playerName())))
                .toList();
    }

    private static String playerKey(String playerName) {
        return playerName.trim().toLowerCase(Locale.ROOT);
    }

    /** Standard competition ranking (1, 1, 3): only an equal score AND equal percent is a tie. */
    private static List<LeaderboardEntryView> assignRanks(List<LeaderboardCandidate> ordered,
                                                          Map<String, Integer> gameCounts) {
        List<LeaderboardEntryView> ranked = new ArrayList<>(ordered.size());
        for (int i = 0; i < ordered.size(); i++) {
            LeaderboardCandidate c = ordered.get(i);
            int rank = i + 1;
            if (i > 0) {
                LeaderboardCandidate previous = ordered.get(i - 1);
                if (previous.score() == c.score()
                        && Objects.equals(previous.percentOfMaxPossible(), c.percentOfMaxPossible())) {
                    rank = ranked.get(i - 1).rank();
                }
            }
            Integer gameCount = gameCounts == null ? null : gameCounts.get(playerKey(c.playerName()));
            ranked.add(new LeaderboardEntryView(
                    rank, c.playerName(), c.score(), c.percentOfMaxPossible(), c.date(), gameCount, c.daily()));
        }
        return ranked;
    }

    private static Comparator<LeaderboardEntryView> columnOrder(LeaderboardSort sort, boolean descending) {
        if (sort == LeaderboardSort.PERCENT || sort == LeaderboardSort.COUNT) {
            // Entries with no value (no recorded max / not grouped by player) stay at the bottom in
            // both directions.
            Comparator<Integer> direction = descending ? Comparator.reverseOrder() : Comparator.naturalOrder();
            return Comparator.comparing(
                    sort == LeaderboardSort.PERCENT
                            ? LeaderboardEntryView::percentOfMaxPossible
                            : LeaderboardEntryView::gameCount,
                    Comparator.nullsLast(direction));
        }
        Comparator<LeaderboardEntryView> ascending = switch (sort) {
            case RANK -> Comparator.comparingInt(LeaderboardEntryView::rank);
            case PLAYER -> Comparator.comparing(LeaderboardEntryView::playerName, String.CASE_INSENSITIVE_ORDER);
            case SCORE -> Comparator.comparingInt(LeaderboardEntryView::score);
            case DATE -> Comparator.comparing(LeaderboardEntryView::date,
                    Comparator.nullsLast(Comparator.naturalOrder()));
            case DAILY -> Comparator.comparing(LeaderboardEntryView::daily);
            case PERCENT, COUNT -> throw new IllegalStateException("handled above");
        };
        return descending ? ascending.reversed() : ascending;
    }
}
