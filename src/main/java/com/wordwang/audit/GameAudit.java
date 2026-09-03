package com.wordwang.audit;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

import java.time.Instant;
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

    @OneToMany(mappedBy = "gameAudit", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlayerAudit> players = new ArrayList<>();

    protected GameAudit() {
    }

    public GameAudit(String gameCode, Instant createdAt, String solutionWord) {
        this.gameCode = gameCode;
        this.createdAt = createdAt;
        this.solutionWord = solutionWord;
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

    public List<PlayerAudit> getPlayers() {
        return players;
    }
}
