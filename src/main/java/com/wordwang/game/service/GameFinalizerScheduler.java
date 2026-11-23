package com.wordwang.game.service;

import com.wordwang.audit.AuditService;
import com.wordwang.daily.DailyChallengeService;
import com.wordwang.game.dto.GameEndedEvent;
import com.wordwang.game.dto.PlayerView;
import com.wordwang.highscore.HighScoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Component
public class GameFinalizerScheduler {

    private static final Logger log = LoggerFactory.getLogger(GameFinalizerScheduler.class);

    private final TaskScheduler taskScheduler;
    private final GameService gameService;
    private final HighScoreService highScoreService;
    private final DailyChallengeService dailyChallengeService;
    private final AuditService auditService;
    private final SimpMessagingTemplate messagingTemplate;
    private final Map<String, ScheduledFuture<?>> scheduledFinalizations = new ConcurrentHashMap<>();
    /** When each pending round is due to end - only used to report how late the end-of-game work ran. */
    private final Map<String, Instant> scheduledEndsAt = new ConcurrentHashMap<>();

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
        scheduledEndsAt.put(gameId, endsAt);
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

    private static long millis(long fromNanos, long toNanos) {
        return (toNanos - fromNanos) / 1_000_000;
    }

    private void finalizeAndBroadcast(String gameId, boolean endedByQuit) {
        long taskStartNanos = System.nanoTime();
        Instant taskStartedAt = Instant.now();
        Instant endsAt = scheduledEndsAt.remove(gameId);
        scheduledFinalizations.remove(gameId);
        gameService.finalizeGame(gameId, endedByQuit).ifPresent(result -> {
            long finalizedNanos = System.nanoTime();
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
            long scoresSavedNanos = System.nanoTime();
            // Scores are saved above, before this broadcast, because the results screen asks for the
            // player's leaderboard rank as soon as it appears. Everything slower must come after it.
            GameEndedEvent event = new GameEndedEvent(result.solutionWord(), result.winners(), result.players(),
                    result.maxPossibleScore(), result.dailyChallengeDate());
            messagingTemplate.convertAndSend("/topic/game/" + gameId, event);
            long broadcastNanos = System.nanoTime();
            Instant broadcastAt = Instant.now();
            log.info("End-of-game timing [game {}]: task started {}ms after endsAt, finalize {}ms, save scores {}ms, "
                            + "broadcast {}ms, broadcast sent {}ms after endsAt ({} players{})",
                    gameId,
                    endsAt == null ? "n/a" : Duration.between(endsAt, taskStartedAt).toMillis(),
                    millis(taskStartNanos, finalizedNanos), millis(finalizedNanos, scoresSavedNanos),
                    millis(scoresSavedNanos, broadcastNanos),
                    endsAt == null ? "n/a" : Duration.between(endsAt, broadcastAt).toMillis(),
                    result.players().size(), endedByQuit ? ", ended by quit" : "");

            // The audit record does a geolocation lookup per player (a live HTTPS call, up to 6s
            // each in the worst case). Do it as its own task so players never wait on it for the
            // results screen - nor does a quitting organiser's request wait on it.
            taskScheduler.schedule(() -> auditService.recordGame(result), Instant.now());
        });
    }
}
