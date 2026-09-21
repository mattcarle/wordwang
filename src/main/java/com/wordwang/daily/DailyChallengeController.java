package com.wordwang.daily;

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
    public List<DailyChallengeView> leaderboard(@RequestParam(required = false) LocalDate date,
                                                 @RequestParam(defaultValue = "20") int limit) {
        LocalDate challengeDate = date != null ? date : dailyChallengeService.today();
        return dailyChallengeService.getLeaderboard(challengeDate, limit);
    }

    @GetMapping("/history")
    public List<DailyHistoryEntryView> history(@RequestParam(defaultValue = "30") int limit) {
        return dailyChallengeService.getHistory(limit);
    }
}
