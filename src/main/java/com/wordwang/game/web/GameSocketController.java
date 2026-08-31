package com.wordwang.game.web;

import com.wordwang.game.dto.GuessFeedbackEvent;
import com.wordwang.game.dto.GuessMessage;
import com.wordwang.game.dto.GuessSubmissionResult;
import com.wordwang.game.dto.ScoreUpdateEvent;
import com.wordwang.game.model.GuessOutcome;
import com.wordwang.game.service.GameService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class GameSocketController {

    private final GameService gameService;
    private final SimpMessagingTemplate messagingTemplate;

    public GameSocketController(GameService gameService, SimpMessagingTemplate messagingTemplate) {
        this.gameService = gameService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/game/{gameId}/guess")
    public void submitGuess(@DestinationVariable String gameId, GuessMessage message) {
        GuessSubmissionResult result = gameService.submitGuess(gameId, message.playerId(), message.word());
        var evaluation = result.evaluation();
        String topic = "/topic/game/" + gameId;
        messagingTemplate.convertAndSend(topic, new GuessFeedbackEvent(
                result.playerId(), evaluation.word(), evaluation.outcome(), evaluation.points(),
                evaluation.message()));
        if (evaluation.outcome() == GuessOutcome.VALID) {
            messagingTemplate.convertAndSend(topic, new ScoreUpdateEvent(result.players()));
        }
    }
}
