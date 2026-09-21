package com.wordwang.game.service;

import com.wordwang.dictionary.DictionaryService;
import com.wordwang.dictionary.LetterMultiset;
import com.wordwang.game.model.GuessOutcome;
import org.springframework.stereotype.Component;

import java.util.Locale;
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
        return LetterMultiset.isSubsetOf(word, LetterMultiset.counts(scrambledWord));
    }
}
