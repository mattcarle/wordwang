package com.wordwang.game.service;

import com.wordwang.game.dto.GameStartedEvent;
import com.wordwang.game.model.Game;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Fires the actual start of a round once the pre-game countdown (broadcast to every player when
 * the organiser hits Start Game) has elapsed.
 */
@Component
public class GameStartScheduler {

    private final TaskScheduler taskScheduler;
    private final GameService gameService;
    private final GameFinalizerScheduler gameFinalizerScheduler;
    private final SimpMessagingTemplate messagingTemplate;

    public GameStartScheduler(TaskScheduler taskScheduler, GameService gameService,
                               GameFinalizerScheduler gameFinalizerScheduler,
                               SimpMessagingTemplate messagingTemplate) {
        this.taskScheduler = taskScheduler;
        this.gameService = gameService;
        this.gameFinalizerScheduler = gameFinalizerScheduler;
        this.messagingTemplate = messagingTemplate;
    }

    public void scheduleRoundStart(String gameId, Instant countdownEndsAt) {
        taskScheduler.schedule(() -> beginRoundAndBroadcast(gameId), countdownEndsAt);
    }

    private void beginRoundAndBroadcast(String gameId) {
        Game game = gameService.beginRound(gameId);
        gameFinalizerScheduler.scheduleFinalization(gameId, game.getEndsAt());
        GameStartedEvent event = new GameStartedEvent(game.getScrambledWord(), game.getEndsAt());
        messagingTemplate.convertAndSend("/topic/game/" + gameId, event);
    }
}
