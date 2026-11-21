package com.wordwang.leaderboard;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

/**
 * How a client wants a leaderboard sliced: which column to sort by, whether a player may appear
 * more than once (when not, only their best entry is kept), and which page of the result to return.
 */
public record LeaderboardQuery(LeaderboardSort sort, boolean descending, boolean uniquePlayers, int offset,
                               int limit) {

    public static final int MAX_LIMIT = 100;

    public LeaderboardQuery {
        if (offset < 0 || limit < 1 || limit > MAX_LIMIT) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "offset must be >= 0 and limit between 1 and " + MAX_LIMIT);
        }
    }

    public static LeaderboardQuery fromParams(String sort, String direction, boolean uniquePlayers, int offset,
                                              int limit) {
        boolean descending = switch (direction.trim().toLowerCase(Locale.ROOT)) {
            case "asc" -> false;
            case "desc" -> true;
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown direction: " + direction);
        };
        return new LeaderboardQuery(LeaderboardSort.fromParam(sort), descending, uniquePlayers, offset, limit);
    }

    /** Rank order, every entry, first page - what the leaderboard shows before anyone touches it. */
    public static LeaderboardQuery firstPage(int limit) {
        return new LeaderboardQuery(LeaderboardSort.RANK, false, false, 0, limit);
    }
}
