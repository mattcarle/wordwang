package com.wordwang.game.dto;

import java.time.LocalDate;
import java.util.List;

public record GameEndedEvent(
        String type, String solutionWord, List<PlayerView> winners, List<PlayerView> players, int maxPossibleScore,
        LocalDate dailyChallengeDate) {

    public GameEndedEvent(String solutionWord, List<PlayerView> winners, List<PlayerView> players,
                           int maxPossibleScore, LocalDate dailyChallengeDate) {
        this("GAME_ENDED", solutionWord, winners, players, maxPossibleScore, dailyChallengeDate);
    }
}
