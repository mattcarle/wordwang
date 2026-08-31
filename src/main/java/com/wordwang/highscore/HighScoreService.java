package com.wordwang.highscore;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class HighScoreService {

    private final HighScoreRepository repository;

    public HighScoreService(HighScoreRepository repository) {
        this.repository = repository;
    }

    public void recordScore(String playerName, int score, String gameId) {
        repository.save(new HighScoreEntry(playerName, score, Instant.now(), gameId));
    }

    public List<HighScoreEntry> getTopScores(int limit) {
        return repository.findAllByOrderByScoreDesc(PageRequest.of(0, limit));
    }
}
