package com.wordwang.audit;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** A permanent record of one played game, written once it finishes - see AuditService.recordGame. */
@Entity
public class GameAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String gameCode;
    private Instant createdAt;
    private String solutionWord;
    private Instant startedAt;
    private Instant finishedAt;
    /** Nullable: rows written before this field existed have no value to backfill it from. */
    private Integer maxPossibleScore;
    /** Null for a normal game; the UTC date of the Daily Wang challenge otherwise. */
    private LocalDate dailyChallengeDate;
    /** Nullable for the same reason as maxPossibleScore. True: organiser quit early. False: timer ran out. */
    private Boolean endedByQuit;

    @OneToMany(mappedBy = "gameAudit", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlayerAudit> players = new ArrayList<>();

    protected GameAudit() {
    }

    public GameAudit(String gameCode, Instant createdAt, String solutionWord, Instant startedAt, Instant finishedAt,
                      Integer maxPossibleScore, LocalDate dailyChallengeDate, Boolean endedByQuit) {
        this.gameCode = gameCode;
        this.createdAt = createdAt;
        this.solutionWord = solutionWord;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
        this.maxPossibleScore = maxPossibleScore;
        this.dailyChallengeDate = dailyChallengeDate;
        this.endedByQuit = endedByQuit;
    }

    public void addPlayer(PlayerAudit player) {
        player.assignGame(this);
        players.add(player);
    }

    public Long getId() {
        return id;
    }

    public String getGameCode() {
        return gameCode;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getSolutionWord() {
        return solutionWord;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getFinishedAt() {
        return finishedAt;
    }

    public Integer getMaxPossibleScore() {
        return maxPossibleScore;
    }

    public LocalDate getDailyChallengeDate() {
        return dailyChallengeDate;
    }

    public Boolean getEndedByQuit() {
        return endedByQuit;
    }

    public List<PlayerAudit> getPlayers() {
        return players;
    }
}
