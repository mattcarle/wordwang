package com.wordwang.highscore;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HighScoreRepository extends JpaRepository<HighScoreEntry, Long> {

    List<HighScoreEntry> findAllByOrderByScoreDesc(Pageable pageable);
}
