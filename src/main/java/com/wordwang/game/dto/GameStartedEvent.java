package com.wordwang.game.dto;

import java.time.Instant;

/**
 * {@code serverTime} is the server's clock at the moment of sending. Clients use it to correct for
 * their own device's clock being off, so the round timer ends when the server ends the round.
 */
public record GameStartedEvent(String type, String scrambledWord, Instant endsAt, Instant serverTime) {

    public GameStartedEvent(String scrambledWord, Instant endsAt) {
        this("GAME_STARTED", scrambledWord, endsAt, Instant.now());
    }
}
