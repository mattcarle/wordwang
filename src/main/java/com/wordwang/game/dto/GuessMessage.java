package com.wordwang.game.dto;

import java.util.UUID;

public record GuessMessage(UUID playerId, String word) {
}
