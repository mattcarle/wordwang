package com.wordwang.game.model;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Player {

    private final UUID id;
    private final String name;
    private final String ipAddress;
    private final Instant joinedAt;
    private int score;
    private final Set<String> foundWords = new HashSet<>();

    public Player(UUID id, String name, String ipAddress) {
        this.id = id;
        this.name = name;
        this.ipAddress = ipAddress;
        this.joinedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public Instant getJoinedAt() {
        return joinedAt;
    }

    public int getScore() {
        return score;
    }

    public void addScore(int points) {
        this.score += points;
    }

    public Set<String> getFoundWords() {
        return foundWords;
    }

    public boolean hasFound(String word) {
        return foundWords.contains(word);
    }

    public void recordFound(String word) {
        foundWords.add(word);
    }
}
