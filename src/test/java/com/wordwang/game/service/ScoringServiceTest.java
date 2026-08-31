package com.wordwang.game.service;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class ScoringServiceTest {

    private final ScoringService scoringService = new ScoringService();

    @ParameterizedTest
    @CsvSource({
            "0, 0",
            "1, 0",
            "2, 0",
            "3, 1",
            "4, 3",
            "5, 7",
            "6, 10",
            "7, 15",
            "8, 20",
            "9, 0",
    })
    void scoreForMatchesPointTable(int length, int expectedPoints) {
        assertThat(scoringService.scoreFor(length)).isEqualTo(expectedPoints);
    }
}
