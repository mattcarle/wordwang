package com.wordwang.game.service;

import com.wordwang.audit.AuditService;
import com.wordwang.daily.DailyChallengeService;
import com.wordwang.game.dto.GameEndedEvent;
import com.wordwang.game.dto.PlayerView;
import com.wordwang.highscore.HighScoreService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Component
public class GameFinalizerScheduler {

    private final TaskScheduler taskScheduler;
    private final GameService gameService;
    private final HighScoreService highScoreService;
    private final DailyChallengeService dailyChallengeService;
    private final AuditService auditService;
    private final SimpMessagingTemplate messagingTemplate;
    private final Map<String, ScheduledFuture<?>> scheduledFinalizations = new ConcurrentHashMap<>();

    public GameFinalizerScheduler(TaskScheduler taskScheduler, GameService gameService,
                                   HighScoreService highScoreService, DailyChallengeService dailyChallengeService,
                                   AuditService auditService, SimpMessagingTemplate messagingTemplate) {
        this.taskScheduler = taskScheduler;
        this.gameService = gameService;
        this.highScoreService = highScoreService;
        this.dailyChallengeService = dailyChallengeService;
        this.auditService = auditService;
        this.messagingTemplate = messagingTemplate;
    }

    public void scheduleFinalization(String gameId, Instant endsAt) {
        ScheduledFuture<?> future = taskScheduler.schedule(() -> finalizeAndBroadcast(gameId, false), endsAt);
        scheduledFinalizations.put(gameId, future);
    }

    /**
     * Ends the round immediately (e.g. the organiser quit the game). Cancels the pending
     * scheduled end-of-round task; {@link GameService#finalizeGame} is idempotent regardless, so
     * this is safe even if the scheduled task is already running.
     */
    public void finalizeNow(String gameId) {
        ScheduledFuture<?> future = scheduledFinalizations.remove(gameId);
        if (future != null) {
            future.cancel(false);
        }
        finalizeAndBroadcast(gameId, true);
    }

    private void finalizeAndBroadcast(String gameId, boolean endedByQuit) {
        scheduledFinalizations.remove(gameId);
        gameService.finalizeGame(gameId, endedByQuit).ifPresent(result -> {
            if (result.dailyChallengeDate() != null) {
                for (PlayerView player : result.players()) {
                    dailyChallengeService.recordCompletion(player.name(), player.score(), result.maxPossibleScore(),
                            result.dailyChallengeDate(), result.solutionWord(), result.dailyPlayerId());
                }
            } else {
                for (PlayerView player : result.players()) {
                    highScoreService.recordScore(player.name(), player.score(), gameId, result.maxPossibleScore());
                }
            }
            auditService.recordGame(result);
            GameEndedEvent event = new GameEndedEvent(result.solutionWord(), result.winners(), result.players(),
                    result.maxPossibleScore(), result.dailyChallengeDate());
            messagingTemplate.convertAndSend("/topic/game/" + gameId, event);
        });
    }
}
