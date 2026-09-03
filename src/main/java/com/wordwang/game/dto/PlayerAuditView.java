package com.wordwang.game.dto;

/**
 * Richer per-player snapshot taken at game-end for the audit trail (see com.wordwang.audit) -
 * distinct from PlayerView, which is the wire-format sent to clients over REST/STOMP.
 */
public record PlayerAuditView(
        String name,
        boolean organiser,
        int score,
        boolean foundEightLetterWord,
        boolean winner,
        String ipAddress) {
}
