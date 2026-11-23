package com.wordwang.game.service;

import com.wordwang.audit.AuditService;
import com.wordwang.daily.DailyChallengeService;
import com.wordwang.game.dto.GameEndResult;
import com.wordwang.game.dto.GameEndedEvent;
import com.wordwang.game.dto.PlayerView;
import com.wordwang.highscore.HighScoreService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.TaskScheduler;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class GameFinalizerSchedulerTest {

    private final TaskScheduler taskScheduler = mock(TaskScheduler.class);
    private final GameService gameService = mock(GameService.class);
    private final HighScoreService highScoreService = mock(HighScoreService.class);
    private final DailyChallengeService dailyChallengeService = mock(DailyChallengeService.class);
    private final AuditService auditService = mock(AuditService.class);
    private final SimpMessagingTemplate messagingTemplate = mock(SimpMessagingTemplate.class);

    private final GameFinalizerScheduler scheduler = new GameFinalizerScheduler(
            taskScheduler, gameService, highScoreService, dailyChallengeService, auditService, messagingTemplate);

    @Test
    void resultsAreBroadcastAfterScoresAreSavedAndWithoutWaitingForTheAudit() {
        PlayerView alice = new PlayerView(UUID.randomUUID(), "Alice", 12);
        GameEndResult result = new GameEndResult("12345", Instant.now(), "GRADIENT", List.of(alice), List.of(alice),
                List.of(), 100, null, null, Instant.now(), Instant.now(), false);
        when(gameService.finalizeGame("12345", true)).thenReturn(Optional.of(result));

        scheduler.finalizeNow("12345");

        // Scores first (the results screen asks for the rank as soon as it appears), then the broadcast.
        InOrder order = inOrder(highScoreService, messagingTemplate, taskScheduler);
        order.verify(highScoreService).recordScore("Alice", 12, "12345", 100);
        order.verify(messagingTemplate).convertAndSend(eq("/topic/game/12345"), any(GameEndedEvent.class));
        order.verify(taskScheduler).schedule(any(Runnable.class), any(Instant.class));

        // The audit (geolocation lookups) is only handed to the scheduler, not run inline...
        verifyNoInteractions(auditService);

        // ...and does run when the scheduled task executes.
        ArgumentCaptor<Runnable> audit = ArgumentCaptor.forClass(Runnable.class);
        verify(taskScheduler).schedule(audit.capture(), any(Instant.class));
        audit.getValue().run();
        verify(auditService).recordGame(result);
    }
}
