package com.wordwang.game.dto;

import com.wordwang.game.model.GameStatus;

import java.util.UUID;

public record CreateGameResponse(String gameId, UUID organiserId, String organiserName, GameStatus status) {
}
