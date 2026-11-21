package com.wordwang.highscore;

import com.wordwang.leaderboard.LeaderboardPageView;
import com.wordwang.leaderboard.LeaderboardQuery;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/highscores")
public class HighScoreController {

    private final HighScoreService highScoreService;

    public HighScoreController(HighScoreService highScoreService) {
        this.highScoreService = highScoreService;
    }

    /** The all-time rank of the score a player recorded in a game; 404 if there is no such score. */
    @GetMapping("/rank")
    public Map<String, Integer> getRank(@RequestParam String gameId, @RequestParam String playerName) {
        int rank = highScoreService.rankOf(gameId, playerName)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No recorded score"));
        return Map.of("rank", rank);
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
