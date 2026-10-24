package com.wordwang.daily;

import com.wordwang.leaderboard.LeaderboardPageView;
import com.wordwang.leaderboard.LeaderboardQuery;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/daily")
public class DailyChallengeController {

    private final DailyChallengeService dailyChallengeService;

    public DailyChallengeController(DailyChallengeService dailyChallengeService) {
        this.dailyChallengeService = dailyChallengeService;
    }

    @GetMapping
    public DailyInfoView today() {
        return new DailyInfoView(dailyChallengeService.today());
    }

    @GetMapping("/leaderboard")
    public LeaderboardPageView leaderboard(@RequestParam(required = false) LocalDate date,
                                           @RequestParam(defaultValue = "rank") String sort,
                                           @RequestParam(defaultValue = "asc") String direction,
                                           @RequestParam(defaultValue = "false") boolean uniquePlayers,
                                           @RequestParam(defaultValue = "0") int offset,
                                           @RequestParam(defaultValue = "20") int limit) {
        LocalDate challengeDate = date != null ? date : dailyChallengeService.today();
        return dailyChallengeService.getLeaderboard(challengeDate,
                LeaderboardQuery.fromParams(sort, direction, uniquePlayers, offset, limit));
    }

    @GetMapping("/history")
    public List<DailyHistoryEntryView> history(@RequestParam(defaultValue = "30") int limit) {
        return dailyChallengeService.getHistory(limit);
    }
}
