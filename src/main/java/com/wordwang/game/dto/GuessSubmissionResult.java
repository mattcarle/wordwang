package com.wordwang.game.dto;

import com.wordwang.game.service.GuessEvaluation;

import java.util.List;
import java.util.UUID;

public record GuessSubmissionResult(UUID playerId, GuessEvaluation evaluation, List<PlayerView> players) {
}
