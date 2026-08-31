package com.wordwang.dictionary;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
}
