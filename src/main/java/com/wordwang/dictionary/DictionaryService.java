package com.wordwang.dictionary;

import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class DictionaryService {

    private static final String RESOURCE_PATH = "dictionary/enable1.txt";
    private static final int GAME_WORD_LENGTH = 8;

    private Set<String> words;
    private List<String> eightLetterWords;

    @PostConstruct
    public void loadDictionary() {
        Set<String> loadedWords = new HashSet<>();
        List<String> loadedEightLetterWords = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new ClassPathResource(RESOURCE_PATH).getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String word = line.trim().toUpperCase(Locale.ROOT);
                if (word.isEmpty()) {
                    continue;
                }
                loadedWords.add(word);
                if (word.length() == GAME_WORD_LENGTH) {
                    loadedEightLetterWords.add(word);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load dictionary resource: " + RESOURCE_PATH, e);
        }

        if (loadedEightLetterWords.isEmpty()) {
            throw new IllegalStateException("Dictionary contains no " + GAME_WORD_LENGTH + "-letter words");
        }

        this.words = Collections.unmodifiableSet(loadedWords);
        this.eightLetterWords = Collections.unmodifiableList(loadedEightLetterWords);
    }

    public boolean isValidWord(String word) {
        return word != null && words.contains(word.toUpperCase(Locale.ROOT));
    }

    public String randomEightLetterWord() {
        int index = ThreadLocalRandom.current().nextInt(eightLetterWords.size());
        return eightLetterWords.get(index);
    }
}
