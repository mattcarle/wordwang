package com.wordwang.dictionary;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/** Shared letter-count logic for checking whether a word can be spelled from a given set of letters. */
public final class LetterMultiset {

    private LetterMultiset() {
    }

    public static Map<Character, Integer> counts(String word) {
        Map<Character, Integer> counts = new HashMap<>();
        for (char c : word.toUpperCase(Locale.ROOT).toCharArray()) {
            counts.merge(c, 1, Integer::sum);
        }
        return counts;
    }

    public static boolean isSubsetOf(String word, Map<Character, Integer> available) {
        Map<Character, Integer> remaining = new HashMap<>(available);
        for (char c : word.toUpperCase(Locale.ROOT).toCharArray()) {
            int left = remaining.getOrDefault(c, 0);
            if (left <= 0) {
                return false;
            }
            remaining.put(c, left - 1);
        }
        return true;
    }
}
