package com.wordwang.game.dto;

import com.wordwang.game.model.GameStatus;

import java.util.List;

public record GameSummaryResponse(String gameId, String organiserName, GameStatus status, List<PlayerView> players) {
}
