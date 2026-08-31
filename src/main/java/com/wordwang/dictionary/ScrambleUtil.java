package com.wordwang.dictionary;

import java.util.concurrent.ThreadLocalRandom;

public final class ScrambleUtil {

    private ScrambleUtil() {
    }

    public static String scramble(String word) {
        char[] letters = word.toCharArray();
        String scrambled;
        do {
            shuffle(letters);
            scrambled = new String(letters);
        } while (scrambled.equals(word) && word.length() > 1);
        return scrambled;
    }

    private static void shuffle(char[] letters) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = letters.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = letters[i];
            letters[i] = letters[j];
            letters[j] = temp;
        }
    }
}
