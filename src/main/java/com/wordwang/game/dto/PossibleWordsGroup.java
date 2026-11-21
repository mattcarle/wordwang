package com.wordwang.game.dto;

import java.util.List;

/** Every possible word of one length for a finished round, alphabetical, with the points each is worth. */
public record PossibleWordsGroup(int length, int pointsPerWord, List<String> words) {
}
