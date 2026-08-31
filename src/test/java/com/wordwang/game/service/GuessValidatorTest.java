package com.wordwang.game.service;

import com.wordwang.dictionary.DictionaryService;
import com.wordwang.game.model.GuessOutcome;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class GuessValidatorTest {

    private static final String SCRAMBLED = "TARDIGEN"; // letters of "GRADIENT"

    private GuessValidator guessValidator;

    @BeforeEach
    void setUp() {
        DictionaryService dictionaryService = new DictionaryService();
        dictionaryService.loadDictionary();
        guessValidator = new GuessValidator(dictionaryService, new ScoringService());
    }

    @Test
    void wordUnderThreeLettersIsTooShort() {
        var result = guessValidator.evaluate("at", SCRAMBLED, Set.of());

        assertThat(result.outcome()).isEqualTo(GuessOutcome.TOO_SHORT);
        assertThat(result.points()).isZero();
        assertThat(result.message()).isEqualTo("AT is too short!");
    }

    @Test
    void wordUsingUnavailableLettersIsRejected() {
        // "GRADIENT" has no letter O
        var result = guessValidator.evaluate("goat", SCRAMBLED, Set.of());

        assertThat(result.outcome()).isEqualTo(GuessOutcome.INVALID_LETTERS);
        assertThat(result.points()).isZero();
    }

    @Test
    void wordReusingALetterMoreTimesThanAvailableIsRejected() {
        // "GRADIENT" has only one letter T
        var result = guessValidator.evaluate("tatting", SCRAMBLED, Set.of());

        assertThat(result.outcome()).isEqualTo(GuessOutcome.INVALID_LETTERS);
    }

    @Test
    void wordNotInDictionaryIsRejected() {
        // "zzz" is long enough and uses only available letters, but isn't a real word
        var result = guessValidator.evaluate("zzz", "ZZZ", Set.of());

        assertThat(result.outcome()).isEqualTo(GuessOutcome.NOT_A_WORD);
        assertThat(result.message()).isEqualTo("ZZZ is not a word!");
    }

    @Test
    void validWordScoresAccordingToLength() {
        var result = guessValidator.evaluate("rating", SCRAMBLED, Set.of());

        assertThat(result.outcome()).isEqualTo(GuessOutcome.VALID);
        assertThat(result.points()).isEqualTo(10);
        assertThat(result.message()).isEqualTo("+10 points!");
    }

    @Test
    void alreadyFoundWordScoresNothing() {
        var result = guessValidator.evaluate("rating", SCRAMBLED, Set.of("RATING"));

        assertThat(result.outcome()).isEqualTo(GuessOutcome.ALREADY_FOUND);
        assertThat(result.points()).isZero();
    }

    @Test
    void guessIsNormalizedToUppercaseAndTrimmed() {
        var result = guessValidator.evaluate("  rating  ", SCRAMBLED, Set.of());

        assertThat(result.word()).isEqualTo("RATING");
    }
}
