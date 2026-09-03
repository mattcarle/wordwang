package com.wordwang.audit;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

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

    protected PlayerAudit() {
    }

    public PlayerAudit(String playerName, boolean organiser, int score, boolean foundEightLetterWord,
                        boolean winner, String ipAddress, String location) {
        this.playerName = playerName;
        this.organiser = organiser;
        this.score = score;
        this.foundEightLetterWord = foundEightLetterWord;
        this.winner = winner;
        this.ipAddress = ipAddress;
        this.location = location;
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
}
