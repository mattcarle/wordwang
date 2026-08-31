package com.wordwang.game.service;

import com.wordwang.dictionary.DictionaryService;
import com.wordwang.dictionary.ScrambleUtil;
import com.wordwang.game.dto.GameEndResult;
import com.wordwang.game.dto.GameSnapshotResponse;
import com.wordwang.game.dto.GameSummaryResponse;
import com.wordwang.game.dto.GuessSubmissionResult;
import com.wordwang.game.dto.PlayerView;
import com.wordwang.game.model.Game;
import com.wordwang.game.model.GameStatus;
import com.wordwang.game.model.Player;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GameService {

    static final Duration ROUND_DURATION = Duration.ofMinutes(2);
    private static final Duration LOBBY_TTL = Duration.ofMinutes(30);
    private static final Duration FINISHED_TTL = Duration.ofMinutes(10);

    private final Map<String, Game> games = new ConcurrentHashMap<>();
    private final DictionaryService dictionaryService;
    private final GameCodeGenerator gameCodeGenerator;
    private final GuessValidator guessValidator;

    public GameService(DictionaryService dictionaryService, GameCodeGenerator gameCodeGenerator,
                        GuessValidator guessValidator) {
        this.dictionaryService = dictionaryService;
        this.gameCodeGenerator = gameCodeGenerator;
        this.guessValidator = guessValidator;
    }

    public Game createGame(String organiserName) {
        String name = requireValidName(organiserName);
        Player organiser = new Player(UUID.randomUUID(), name);
        String gameId = gameCodeGenerator.generate(games::containsKey);
        Game game = new Game(gameId, organiser);
        games.put(gameId, game);
        return game;
    }

    public Player joinGame(String gameId, String playerName) {
        Game game = requireGame(gameId);
        String name = requireValidName(playerName);
        synchronized (game) {
            if (game.getStatus() != GameStatus.LOBBY) {
                throw new IllegalStateException("Game " + gameId + " has already started");
            }
            if (game.hasPlayerNamed(name)) {
                throw new IllegalArgumentException("A player named \"" + name + "\" has already joined this game");
            }
            Player player = new Player(UUID.randomUUID(), name);
            game.getPlayers().put(player.getId(), player);
            return player;
        }
    }

    public Game startGame(String gameId, UUID requestingPlayerId) {
        Game game = requireGame(gameId);
        synchronized (game) {
            if (!game.isOrganiser(requestingPlayerId)) {
                throw new ForbiddenException("Only the organiser can start the game");
            }
            if (game.getStatus() != GameStatus.LOBBY) {
                throw new IllegalStateException("Game " + gameId + " is not waiting to start");
            }
            String solution = dictionaryService.randomEightLetterWord();
            game.setSolutionWord(solution);
            game.setScrambledWord(ScrambleUtil.scramble(solution));
            Instant now = Instant.now();
            game.setStartedAt(now);
            game.setEndsAt(now.plus(ROUND_DURATION));
            game.setStatus(GameStatus.IN_PROGRESS);
        }
        return game;
    }

    public GuessSubmissionResult submitGuess(String gameId, UUID playerId, String rawWord) {
        Game game = requireGame(gameId);
        synchronized (game) {
            if (game.getStatus() != GameStatus.IN_PROGRESS) {
                throw new IllegalStateException("Game " + gameId + " is not in progress");
            }
            Player player = game.getPlayer(playerId);
            if (player == null) {
                throw new ForbiddenException("Player is not part of this game");
            }
            var evaluation = guessValidator.evaluate(rawWord, game.getScrambledWord(), player.getFoundWords());
            if (evaluation.scored()) {
                player.addScore(evaluation.points());
                player.recordFound(evaluation.word());
            }
            return new GuessSubmissionResult(playerId, evaluation, sortedPlayerViews(game));
        }
    }

    /**
     * Transitions the game to FINISHED and computes the result, but only the first caller for a
     * given game gets a result back — later calls (e.g. a race between the scheduled end-of-round
     * task and an organiser quitting early) return empty so the game only gets finalized once.
     */
    public Optional<GameEndResult> finalizeGame(String gameId) {
        Game game = requireGame(gameId);
        synchronized (game) {
            if (game.getStatus() == GameStatus.FINISHED) {
                return Optional.empty();
            }
            game.setStatus(GameStatus.FINISHED);
            game.setFinishedAt(Instant.now());
            List<PlayerView> players = sortedPlayerViews(game);
            List<PlayerView> winners = computeWinners(players);
            return Optional.of(new GameEndResult(game.getId(), game.getSolutionWord(), winners, players));
        }
    }

    public void requestQuit(String gameId, UUID requestingPlayerId) {
        Game game = requireGame(gameId);
        synchronized (game) {
            if (!game.isOrganiser(requestingPlayerId)) {
                throw new ForbiddenException("Only the organiser can end the game");
            }
            if (game.getStatus() != GameStatus.IN_PROGRESS) {
                throw new IllegalStateException("Game " + gameId + " is not in progress");
            }
        }
    }

    public GameSummaryResponse getSummary(String gameId) {
        Game game = requireGame(gameId);
        synchronized (game) {
            Player organiser = game.getPlayer(game.getOrganiserId());
            String organiserName = organiser == null ? "" : organiser.getName();
            return new GameSummaryResponse(game.getId(), organiserName, game.getStatus(), sortedPlayerViews(game));
        }
    }

    public GameSnapshotResponse getSnapshot(String gameId, UUID playerId) {
        Game game = requireGame(gameId);
        synchronized (game) {
            Player player = playerId == null ? null : game.getPlayer(playerId);
            boolean finished = game.getStatus() == GameStatus.FINISHED;
            String solutionWord = finished ? game.getSolutionWord() : null;
            List<String> yourFoundWords = player == null
                    ? Collections.emptyList()
                    : List.copyOf(player.getFoundWords());
            List<PlayerView> players = sortedPlayerViews(game);
            List<PlayerView> winners = finished ? computeWinners(players) : Collections.emptyList();
            return new GameSnapshotResponse(
                    game.getId(),
                    game.getStatus(),
                    game.getOrganiserId(),
                    game.getScrambledWord(),
                    solutionWord,
                    game.getEndsAt(),
                    players,
                    yourFoundWords,
                    winners);
        }
    }

    @Scheduled(fixedRate = 60_000)
    void cleanupAbandonedGames() {
        Instant now = Instant.now();
        games.values().removeIf(game -> {
            synchronized (game) {
                if (game.getStatus() == GameStatus.LOBBY) {
                    return Duration.between(game.getCreatedAt(), now).compareTo(LOBBY_TTL) > 0;
                }
                if (game.getStatus() == GameStatus.FINISHED && game.getFinishedAt() != null) {
                    return Duration.between(game.getFinishedAt(), now).compareTo(FINISHED_TTL) > 0;
                }
                return false;
            }
        });
    }

    private List<PlayerView> computeWinners(List<PlayerView> players) {
        int topScore = players.stream().mapToInt(PlayerView::score).max().orElse(0);
        return players.stream().filter(p -> p.score() == topScore).toList();
    }

    private List<PlayerView> sortedPlayerViews(Game game) {
        List<Player> players = new ArrayList<>(game.playerList());
        players.sort(Comparator.comparingInt(Player::getScore).reversed()
                .thenComparing(Player::getJoinedAt));
        return players.stream().map(PlayerView::from).toList();
    }

    private Game requireGame(String gameId) {
        Game game = games.get(gameId);
        if (game == null) {
            throw new GameNotFoundException(gameId);
        }
        return game;
    }

    private String requireValidName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("A name is required");
        }
        return name.trim();
    }
}
