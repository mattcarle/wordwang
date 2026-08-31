package com.wordwang.game.dto;

import java.util.List;

public record ScoreUpdateEvent(String type, List<PlayerView> players) {

    public ScoreUpdateEvent(List<PlayerView> players) {
        this("SCORE_UPDATE", players);
    }
}
