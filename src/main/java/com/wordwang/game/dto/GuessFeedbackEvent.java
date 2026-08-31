package com.wordwang.game.dto;

import com.wordwang.game.model.GuessOutcome;

import java.util.UUID;

public record GuessFeedbackEvent(
        String type, UUID playerId, String word, GuessOutcome outcome, int points, String message) {

    public GuessFeedbackEvent(UUID playerId, String word, GuessOutcome outcome, int points, String message) {
        this("GUESS_FEEDBACK", playerId, word, outcome, points, message);
    }
}
