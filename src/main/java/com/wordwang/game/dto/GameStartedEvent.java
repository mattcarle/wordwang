package com.wordwang.game.dto;

import java.time.Instant;

public record GameStartedEvent(String type, String scrambledWord, Instant endsAt) {

    public GameStartedEvent(String scrambledWord, Instant endsAt) {
        this("GAME_STARTED", scrambledWord, endsAt);
    }
}
