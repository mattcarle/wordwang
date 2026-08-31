package com.wordwang.game.service;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GameCodeGeneratorTest {

    private final GameCodeGenerator generator = new GameCodeGenerator();

    @Test
    void generatesAFiveDigitCode() {
        String code = generator.generate(taken -> false);

        assertThat(code).hasSize(5);
        assertThat(Integer.parseInt(code)).isBetween(10_000, 99_999);
    }

    @Test
    void retriesUntilAnUntakenCodeIsFound() {
        Set<String> taken = Set.of("11111", "22222", "33333");

        String code = generator.generate(taken::contains);

        assertThat(taken).doesNotContain(code);
    }

    @Test
    void givesUpAfterTooManyCollisions() {
        assertThatThrownBy(() -> generator.generate(code -> true))
                .isInstanceOf(IllegalStateException.class);
    }
}
