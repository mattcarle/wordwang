package com.wordwang.game.dto;

import java.util.List;

public record GameEndedEvent(String type, String solutionWord, List<PlayerView> winners, List<PlayerView> players) {

    public GameEndedEvent(String solutionWord, List<PlayerView> winners, List<PlayerView> players) {
        this("GAME_ENDED", solutionWord, winners, players);
    }
}
