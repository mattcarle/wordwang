package com.wordwang.daily;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DailyChallengeRepository extends JpaRepository<DailyChallengeEntry, Long> {

    List<DailyChallengeEntry> findByChallengeDateOrderByScoreDesc(LocalDate challengeDate, Pageable pageable);

    Optional<DailyChallengeEntry> findByChallengeDateAndDailyPlayerId(LocalDate challengeDate, UUID dailyPlayerId);

    @Query("""
            SELECT e.challengeDate AS challengeDate, e.solutionWord AS solutionWord
            FROM DailyChallengeEntry e
            WHERE e.challengeDate < :today
            GROUP BY e.challengeDate, e.solutionWord
            ORDER BY e.challengeDate DESC
            """)
    List<DailyHistoryRow> findPastChallengeDays(@Param("today") LocalDate today, Pageable pageable);

    interface DailyHistoryRow {
        LocalDate getChallengeDate();

        String getSolutionWord();
    }
}
