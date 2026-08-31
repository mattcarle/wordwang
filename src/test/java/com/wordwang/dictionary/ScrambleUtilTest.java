package com.wordwang.dictionary;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ScrambleUtilTest {

    @RepeatedTest(20)
    void scrambledWordIsAPermutationOfTheInput() {
        String word = "SCRAMBLE";
        String scrambled = ScrambleUtil.scramble(word);

        assertThat(scrambled).hasSameSizeAs(word);
        assertThat(sortedChars(scrambled)).isEqualTo(sortedChars(word));
    }

    @Test
    void singleLetterWordIsReturnedAsIs() {
        assertThat(ScrambleUtil.scramble("A")).isEqualTo("A");
    }

    private char[] sortedChars(String word) {
        char[] chars = word.toCharArray();
        java.util.Arrays.sort(chars);
        return chars;
    }
}
