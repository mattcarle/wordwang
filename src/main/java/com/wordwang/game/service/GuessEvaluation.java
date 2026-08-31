package com.wordwang.game.service;

import com.wordwang.game.model.GuessOutcome;

public record GuessEvaluation(String word, GuessOutcome outcome, int points, String message) {

    public boolean scored() {
        return outcome == GuessOutcome.VALID;
    }
}
