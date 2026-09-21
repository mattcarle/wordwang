package com.wordwang.game.dto;

import java.util.UUID;

/** {@code dailyPlayerId} is only used by {@code POST /api/games/daily} - ignored for a normal game. */
public record CreateGameRequest(String organiserName, UUID dailyPlayerId) {
}
