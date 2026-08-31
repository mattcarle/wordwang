package com.wordwang.game.service;

import com.wordwang.dictionary.DictionaryService;
import com.wordwang.game.model.GuessOutcome;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Component
public class GuessValidator {

    private final DictionaryService dictionaryService;
    private final ScoringService scoringService;

    public GuessValidator(DictionaryService dictionaryService, ScoringService scoringService) {
        this.dictionaryService = dictionaryService;
        this.scoringService = scoringService;
    }

    public GuessEvaluation evaluate(String rawGuess, String scrambledWord, Set<String> alreadyFound) {
        String word = rawGuess == null ? "" : rawGuess.trim().toUpperCase(Locale.ROOT);

        if (word.length() < 3) {
            return new GuessEvaluation(word, GuessOutcome.TOO_SHORT, 0, word + " is too short!");
        }

        if (!usesOnlyAvailableLetters(word, scrambledWord)) {
            return new GuessEvaluation(word, GuessOutcome.INVALID_LETTERS, 0,
                    word + " can't be made from these letters!");
        }

        if (!dictionaryService.isValidWord(word)) {
            return new GuessEvaluation(word, GuessOutcome.NOT_A_WORD, 0, word + " is not a word!");
        }

        if (alreadyFound.contains(word)) {
            return new GuessEvaluation(word, GuessOutcome.ALREADY_FOUND, 0,
                    "You've already found " + word + "!");
        }

        int points = scoringService.scoreFor(word.length());
        return new GuessEvaluation(word, GuessOutcome.VALID, points, "+" + points + " points!");
    }

    private boolean usesOnlyAvailableLetters(String word, String scrambledWord) {
        Map<Character, Integer> available = letterCounts(scrambledWord);
        for (char c : word.toCharArray()) {
            int remaining = available.getOrDefault(c, 0);
            if (remaining <= 0) {
                return false;
            }
            available.put(c, remaining - 1);
        }
        return true;
    }

    private Map<Character, Integer> letterCounts(String word) {
        Map<Character, Integer> counts = new HashMap<>();
        for (char c : word.toUpperCase(Locale.ROOT).toCharArray()) {
            counts.merge(c, 1, Integer::sum);
        }
        return counts;
    }
}
