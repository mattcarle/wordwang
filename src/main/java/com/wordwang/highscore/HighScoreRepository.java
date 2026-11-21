package com.wordwang.highscore;

import org.springframework.data.jpa.repository.JpaRepository;

public interface HighScoreRepository extends JpaRepository<HighScoreEntry, Long> {
}
