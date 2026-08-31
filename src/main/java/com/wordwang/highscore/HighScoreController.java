package com.wordwang.highscore;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/highscores")
public class HighScoreController {

    private final HighScoreService highScoreService;

    public HighScoreController(HighScoreService highScoreService) {
        this.highScoreService = highScoreService;
    }

    @GetMapping
    public List<HighScoreView> getTopScores(@RequestParam(defaultValue = "20") int limit) {
        return highScoreService.getTopScores(limit).stream().map(HighScoreView::from).toList();
    }
}
