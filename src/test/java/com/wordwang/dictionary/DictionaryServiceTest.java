package com.wordwang.dictionary;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class DictionaryServiceTest {

    private DictionaryService dictionaryService;

    @BeforeEach
    void setUp() {
        dictionaryService = new DictionaryService();
        dictionaryService.loadDictionary();
    }

    @Test
    void recognisesKnownWordsCaseInsensitively() {
        assertThat(dictionaryService.isValidWord("cat")).isTrue();
        assertThat(dictionaryService.isValidWord("CAT")).isTrue();
        assertThat(dictionaryService.isValidWord("Cat")).isTrue();
    }

    @Test
    void rejectsGibberish() {
        assertThat(dictionaryService.isValidWord("zzqxxpp")).isFalse();
        assertThat(dictionaryService.isValidWord(null)).isFalse();
    }

    @Test
    void randomEightLetterWordIsAlwaysEightLettersAndValid() {
        for (int i = 0; i < 50; i++) {
            String word = dictionaryService.randomEightLetterWord();
            assertThat(word).hasSize(8);
            assertThat(dictionaryService.isValidWord(word)).isTrue();
        }
    }

    @Test
    void wordsUsingLettersFindsEveryValidWordWithinTheLetterCounts() {
        List<String> words = dictionaryService.wordsUsingLetters("TARDIGEN"); // letters of GRADIENT

        assertThat(words).contains("GRADIENT", "RATING", "TRADE", "RAT", "TAG");
        assertThat(words).allSatisfy(word -> assertThat(word.length()).isGreaterThanOrEqualTo(3));
        assertThat(words).doesNotContain("GRADIENTS"); // needs a second S not present in the letters
    }

    @Test
    void wordsUsingLettersRespectsLetterCounts() {
        // Only one "T" available, so a word needing two can't be formed even though both letters exist.
        assertThat(dictionaryService.wordsUsingLetters("TARDIGEN")).doesNotContain("TATTER");
    }

    @Test
    void dailyWordIsStableForTheSameDateAndValid() {
        LocalDate date = LocalDate.of(2026, 9, 16);

        String first = dictionaryService.dailyWord(date);
        String second = dictionaryService.dailyWord(date);

        assertThat(first).isEqualTo(second);
        assertThat(first).hasSize(8);
        assertThat(dictionaryService.isValidWord(first)).isTrue();
    }

    @Test
    void dailyWordCanDifferAcrossDates() {
        List<String> words = IntStream.range(0, 10)
                .mapToObj(i -> dictionaryService.dailyWord(LocalDate.of(2026, 1, 1).plusDays(i)))
                .distinct()
                .toList();

        assertThat(words).hasSizeGreaterThan(1);
    }
}
