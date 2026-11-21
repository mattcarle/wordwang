package com.wordwang.leaderboard;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

/** The leaderboard column a page is sorted by - one per table column on the frontend. */
public enum LeaderboardSort {
    RANK, PLAYER, SCORE, PERCENT, DATE;

    public static LeaderboardSort fromParam(String value) {
        try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown sort: " + value);
        }
    }
}
