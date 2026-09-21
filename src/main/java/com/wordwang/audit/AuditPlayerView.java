package com.wordwang.audit;

import java.util.List;

public record AuditPlayerView(
        String name,
        boolean organiser,
        int score,
        boolean foundEightLetterWord,
        boolean winner,
        String ipAddress,
        String location,
        List<String> foundWords) {

    static AuditPlayerView from(PlayerAudit player) {
        return new AuditPlayerView(
                player.getPlayerName(),
                player.isOrganiser(),
                player.getScore(),
                player.isFoundEightLetterWord(),
                player.isWinner(),
                player.getIpAddress(),
                player.getLocation(),
                player.getFoundWords());
    }
}
