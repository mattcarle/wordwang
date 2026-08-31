package com.wordwang.game.model;

import java.time.Instant;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class Game {

    private final String id;
    private final UUID organiserId;
    private final Instant createdAt = Instant.now();
    private final Map<UUID, Player> players = new LinkedHashMap<>();

    private volatile GameStatus status = GameStatus.LOBBY;
    private String solutionWord;
    private String scrambledWord;
    private Instant startedAt;
    private Instant endsAt;
    private Instant finishedAt;

    public Game(String id, Player organiser) {
        this.id = id;
        this.organiserId = organiser.getId();
        this.players.put(organiser.getId(), organiser);
    }

    public String getId() {
        return id;
    }

    public UUID getOrganiserId() {
        return organiserId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public GameStatus getStatus() {
        return status;
    }

    public void setStatus(GameStatus status) {
        this.status = status;
    }

    public String getSolutionWord() {
        return solutionWord;
    }

    public void setSolutionWord(String solutionWord) {
        this.solutionWord = solutionWord;
    }

    public String getScrambledWord() {
        return scrambledWord;
    }

    public void setScrambledWord(String scrambledWord) {
        this.scrambledWord = scrambledWord;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getEndsAt() {
        return endsAt;
    }

    public void setEndsAt(Instant endsAt) {
        this.endsAt = endsAt;
    }

    public Instant getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(Instant finishedAt) {
        this.finishedAt = finishedAt;
    }

    public Map<UUID, Player> getPlayers() {
        return players;
    }

    public Collection<Player> playerList() {
        return players.values();
    }

    public Player getPlayer(UUID playerId) {
        return players.get(playerId);
    }

    public boolean isOrganiser(UUID playerId) {
        return organiserId.equals(playerId);
    }

    public boolean hasPlayerNamed(String name) {
        return players.values().stream()
                .anyMatch(p -> p.getName().equalsIgnoreCase(name));
    }
}
