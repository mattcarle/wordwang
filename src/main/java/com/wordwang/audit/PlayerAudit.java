package com.wordwang.audit;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderColumn;

import java.util.ArrayList;
import java.util.List;

@Entity
public class PlayerAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "game_audit_id", nullable = false)
    private GameAudit gameAudit;

    private String playerName;
    private boolean organiser;
    private int score;
    private boolean foundEightLetterWord;
    private boolean winner;
    private String ipAddress;
    private String location;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "player_audit_found_word", joinColumns = @JoinColumn(name = "player_audit_id"))
    @OrderColumn(name = "position")
    @Column(name = "word")
    private List<String> foundWords = new ArrayList<>();

    protected PlayerAudit() {
    }

    public PlayerAudit(String playerName, boolean organiser, int score, boolean foundEightLetterWord,
                        boolean winner, String ipAddress, String location, List<String> foundWords) {
        this.playerName = playerName;
        this.organiser = organiser;
        this.score = score;
        this.foundEightLetterWord = foundEightLetterWord;
        this.winner = winner;
        this.ipAddress = ipAddress;
        this.location = location;
        this.foundWords = foundWords;
    }

    void assignGame(GameAudit gameAudit) {
        this.gameAudit = gameAudit;
    }

    public Long getId() {
        return id;
    }

    public String getPlayerName() {
        return playerName;
    }

    public boolean isOrganiser() {
        return organiser;
    }

    public int getScore() {
        return score;
    }

    public boolean isFoundEightLetterWord() {
        return foundEightLetterWord;
    }

    public boolean isWinner() {
        return winner;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getLocation() {
        return location;
    }

    public List<String> getFoundWords() {
        return foundWords;
    }
}
