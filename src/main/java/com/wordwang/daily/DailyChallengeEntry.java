package com.wordwang.daily;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
public class DailyChallengeEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate challengeDate;
    /**
     * Denormalized onto every row (rather than re-derived from the date via DictionaryService)
     * so a past day's word stays correct forever even if the dictionary or hashing scheme changes.
     */
    private String solutionWord;
    private String playerName;
    private int score;
    private int maxPossibleScore;
    private Instant completedAt;
    /** Nullable: only set when the client sent a daily-player identity (see GameService#createDailyGame). */
    private UUID dailyPlayerId;

    protected DailyChallengeEntry() {
    }

    public DailyChallengeEntry(LocalDate challengeDate, String solutionWord, String playerName, int score,
                                int maxPossibleScore, Instant completedAt) {
        this(challengeDate, solutionWord, playerName, score, maxPossibleScore, completedAt, null);
    }

    public DailyChallengeEntry(LocalDate challengeDate, String solutionWord, String playerName, int score,
                                int maxPossibleScore, Instant completedAt, UUID dailyPlayerId) {
        this.challengeDate = challengeDate;
        this.solutionWord = solutionWord;
        this.playerName = playerName;
        this.score = score;
        this.maxPossibleScore = maxPossibleScore;
        this.completedAt = completedAt;
        this.dailyPlayerId = dailyPlayerId;
    }

    public Long getId() {
        return id;
    }

    public LocalDate getChallengeDate() {
        return challengeDate;
    }

    public String getSolutionWord() {
        return solutionWord;
    }

    public String getPlayerName() {
        return playerName;
    }

    public int getScore() {
        return score;
    }

    public int getMaxPossibleScore() {
        return maxPossibleScore;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public UUID getDailyPlayerId() {
        return dailyPlayerId;
    }
}
