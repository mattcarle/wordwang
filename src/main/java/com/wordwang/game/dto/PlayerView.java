package com.wordwang.game.dto;

import com.wordwang.game.model.Player;

import java.util.UUID;

public record PlayerView(UUID playerId, String name, int score) {

    public static PlayerView from(Player player) {
        return new PlayerView(player.getId(), player.getName(), player.getScore());
    }
}
