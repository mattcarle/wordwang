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

    protected HighScoreEntry() {
    }

    public HighScoreEntry(String playerName, int score, Instant playedAt, String gameId) {
        this.playerName = playerName;
        this.score = score;
        this.playedAt = playedAt;
        this.gameId = gameId;
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
}
