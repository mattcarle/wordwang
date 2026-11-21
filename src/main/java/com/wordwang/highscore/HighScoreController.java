package com.wordwang.highscore;

import com.wordwang.leaderboard.LeaderboardPageView;
import com.wordwang.leaderboard.LeaderboardQuery;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/highscores")
public class HighScoreController {

    private final HighScoreService highScoreService;

    public HighScoreController(HighScoreService highScoreService) {
        this.highScoreService = highScoreService;
    }

    @GetMapping
    public LeaderboardPageView getLeaderboard(@RequestParam(defaultValue = "rank") String sort,
                                              @RequestParam(defaultValue = "asc") String direction,
                                              @RequestParam(defaultValue = "false") boolean uniquePlayers,
                                              @RequestParam(defaultValue = "0") int offset,
                                              @RequestParam(defaultValue = "20") int limit) {
        return highScoreService.getLeaderboard(
                LeaderboardQuery.fromParams(sort, direction, uniquePlayers, offset, limit));
    }
}
