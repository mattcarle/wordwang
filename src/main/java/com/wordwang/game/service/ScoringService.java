package com.wordwang.game.service;

import org.springframework.stereotype.Component;

@Component
public class ScoringService {

    public int scoreFor(int wordLength) {
        return switch (wordLength) {
            case 3 -> 1;
            case 4 -> 3;
            case 5 -> 7;
            case 6 -> 10;
            case 7 -> 15;
            case 8 -> 20;
            default -> 0;
        };
    }
}
