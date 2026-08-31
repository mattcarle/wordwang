package com.wordwang.game.service;

import com.wordwang.game.dto.GameEndResult;
import com.wordwang.game.dto.GameEndedEvent;
import com.wordwang.game.dto.PlayerView;
import com.wordwang.highscore.HighScoreService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class GameFinalizerScheduler {

    private final TaskScheduler taskScheduler;
    private final GameService gameService;
    private final HighScoreService highScoreService;
    private final SimpMessagingTemplate messagingTemplate;

    public GameFinalizerScheduler(TaskScheduler taskScheduler, GameService gameService,
                                   HighScoreService highScoreService, SimpMessagingTemplate messagingTemplate) {
        this.taskScheduler = taskScheduler;
        this.gameService = gameService;
        this.highScoreService = highScoreService;
        this.messagingTemplate = messagingTemplate;
    }

    public void scheduleFinalization(String gameId, Instant endsAt) {
        taskScheduler.schedule(() -> finalizeAndBroadcast(gameId), endsAt);
    }

    private void finalizeAndBroadcast(String gameId) {
        GameEndResult result = gameService.finalizeGame(gameId);
        for (PlayerView player : result.players()) {
            highScoreService.recordScore(player.name(), player.score(), gameId);
        }
        GameEndedEvent event = new GameEndedEvent(result.solutionWord(), result.winners(), result.players());
        messagingTemplate.convertAndSend("/topic/game/" + gameId, event);
    }
}
