package com.wordwang.game.dto;

import com.wordwang.game.model.GameStatus;

import java.util.List;
import java.util.UUID;

public record JoinGameResponse(String gameId, UUID playerId, GameStatus status, List<PlayerView> players) {
}
