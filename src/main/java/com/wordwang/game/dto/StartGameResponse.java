package com.wordwang.game.dto;

import com.wordwang.game.model.GameStatus;

import java.time.Instant;

public record StartGameResponse(String gameId, GameStatus status, String scrambledWord, Instant endsAt) {
}
