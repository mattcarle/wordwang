package com.wordwang.game.dto;

import java.util.List;

public record PlayerJoinedEvent(String type, List<PlayerView> players) {

    public PlayerJoinedEvent(List<PlayerView> players) {
        this("PLAYER_JOINED", players);
    }
}
