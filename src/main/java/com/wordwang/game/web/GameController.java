package com.wordwang.game.web;

import com.wordwang.game.dto.CreateGameRequest;
import com.wordwang.game.dto.CreateGameResponse;
import com.wordwang.game.dto.GameSnapshotResponse;
import com.wordwang.game.dto.GameStartedEvent;
import com.wordwang.game.dto.GameSummaryResponse;
import com.wordwang.game.dto.JoinGameRequest;
import com.wordwang.game.dto.JoinGameResponse;
import com.wordwang.game.dto.PlayerJoinedEvent;
import com.wordwang.game.dto.StartGameRequest;
import com.wordwang.game.dto.StartGameResponse;
import com.wordwang.game.model.Game;
import com.wordwang.game.model.Player;
import com.wordwang.game.service.GameFinalizerScheduler;
import com.wordwang.game.service.GameService;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/games")
public class GameController {

    private final GameService gameService;
    private final GameFinalizerScheduler gameFinalizerScheduler;
    private final SimpMessagingTemplate messagingTemplate;

    public GameController(GameService gameService, GameFinalizerScheduler gameFinalizerScheduler,
                           SimpMessagingTemplate messagingTemplate) {
        this.gameService = gameService;
        this.gameFinalizerScheduler = gameFinalizerScheduler;
        this.messagingTemplate = messagingTemplate;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateGameResponse createGame(@RequestBody CreateGameRequest request) {
        Game game = gameService.createGame(request.organiserName());
        Player organiser = game.getPlayer(game.getOrganiserId());
        return new CreateGameResponse(game.getId(), organiser.getId(), organiser.getName(), game.getStatus());
    }

    @GetMapping("/{gameId}")
    public GameSummaryResponse getSummary(@PathVariable String gameId) {
        return gameService.getSummary(gameId);
    }

    @GetMapping("/{gameId}/snapshot")
    public GameSnapshotResponse getSnapshot(@PathVariable String gameId,
                                             @RequestParam(required = false) UUID playerId) {
        return gameService.getSnapshot(gameId, playerId);
    }

    @PostMapping("/{gameId}/join")
    public JoinGameResponse join(@PathVariable String gameId, @RequestBody JoinGameRequest request) {
        Player joined = gameService.joinGame(gameId, request.playerName());
        GameSummaryResponse summary = gameService.getSummary(gameId);
        messagingTemplate.convertAndSend("/topic/game/" + gameId, new PlayerJoinedEvent(summary.players()));
        return new JoinGameResponse(gameId, joined.getId(), summary.status(), summary.players());
    }

    @PostMapping("/{gameId}/start")
    public StartGameResponse start(@PathVariable String gameId, @RequestBody StartGameRequest request) {
        Game game = gameService.startGame(gameId, request.playerId());
        gameFinalizerScheduler.scheduleFinalization(gameId, game.getEndsAt());
        GameStartedEvent event = new GameStartedEvent(game.getScrambledWord(), game.getEndsAt());
        messagingTemplate.convertAndSend("/topic/game/" + gameId, event);
        return new StartGameResponse(gameId, game.getStatus(), game.getScrambledWord(), game.getEndsAt());
    }
}
