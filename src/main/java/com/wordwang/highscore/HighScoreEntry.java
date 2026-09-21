package com.wordwang.highscore;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.Instant;

@Entity
public class HighScoreEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String playerName;
    private int score;
    private Instant playedAt;
    private String gameId;
    /** Nullable: entries recorded before this field existed have no value to backfill it from. */
    private Integer maxPossibleScore;

    protected HighScoreEntry() {
    }

    public HighScoreEntry(String playerName, int score, Instant playedAt, String gameId, Integer maxPossibleScore) {
        this.playerName = playerName;
        this.score = score;
        this.playedAt = playedAt;
        this.gameId = gameId;
        this.maxPossibleScore = maxPossibleScore;
    }

    public Long getId() {
        return id;
    }

    public String getPlayerName() {
        return playerName;
    }

    public int getScore() {
        return score;
    }

    public Instant getPlayedAt() {
        return playedAt;
    }

    public String getGameId() {
        return gameId;
    }

    public Integer getMaxPossibleScore() {
        return maxPossibleScore;
    }
}
