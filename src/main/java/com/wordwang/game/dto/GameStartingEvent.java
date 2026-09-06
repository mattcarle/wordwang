package com.wordwang.game.dto;

import java.time.Instant;

public record GameStartingEvent(String type, Instant countdownEndsAt) {

    public GameStartingEvent(Instant countdownEndsAt) {
        this("GAME_STARTING", countdownEndsAt);
    }
}
