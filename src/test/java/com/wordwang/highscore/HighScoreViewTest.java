package com.wordwang.highscore;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class HighScoreViewTest {

    @Test
    void computesPercentOfMaxPossibleScore() {
        HighScoreEntry entry = new HighScoreEntry("Alice", 25, Instant.now(), "12345", 100);

        HighScoreView view = HighScoreView.from(entry);

        assertThat(view.percentOfMaxPossible()).isEqualTo(25);
    }

    @Test
    void percentIsNullWhenMaxPossibleScoreWasNeverRecorded() {
        HighScoreEntry entry = new HighScoreEntry("Alice", 25, Instant.now(), "12345", null);

        HighScoreView view = HighScoreView.from(entry);

        assertThat(view.percentOfMaxPossible()).isNull();
    }

    @Test
    void percentIsNullRatherThanDivideByZeroWhenMaxPossibleScoreIsZero() {
        HighScoreEntry entry = new HighScoreEntry("Alice", 0, Instant.now(), "12345", 0);

        HighScoreView view = HighScoreView.from(entry);

        assertThat(view.percentOfMaxPossible()).isNull();
    }
}
