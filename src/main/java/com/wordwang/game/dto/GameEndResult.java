package com.wordwang.game.dto;

import java.util.List;

public record GameEndResult(String gameId, String solutionWord, List<PlayerView> winners, List<PlayerView> players) {
}
