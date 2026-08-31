package com.wordwang.game.service;

import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;

@Component
public class GameCodeGenerator {

    private static final int MIN_CODE = 10_000;
    private static final int MAX_CODE_EXCLUSIVE = 100_000;
    private static final int MAX_ATTEMPTS = 20;

    public String generate(Predicate<String> isTaken) {
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            String code = String.valueOf(ThreadLocalRandom.current().nextInt(MIN_CODE, MAX_CODE_EXCLUSIVE));
            if (!isTaken.test(code)) {
                return code;
            }
        }
        throw new IllegalStateException("Unable to generate a unique game code after " + MAX_ATTEMPTS + " attempts");
    }
}
