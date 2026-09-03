package com.wordwang.game.service;

import com.wordwang.dictionary.DictionaryService;
import com.wordwang.game.dto.GameEndResult;
import com.wordwang.game.dto.GuessSubmissionResult;
import com.wordwang.game.dto.PlayerAuditView;
import com.wordwang.game.model.Game;
import com.wordwang.game.model.GameStatus;
import com.wordwang.game.model.GuessOutcome;
import com.wordwang.game.model.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GameServiceTest {

    private GameService gameService;

    @BeforeEach
    void setUp() {
        DictionaryService dictionaryService = new DictionaryService();
        dictionaryService.loadDictionary();
        gameService = new GameService(dictionaryService, new GameCodeGenerator(),
                new GuessValidator(dictionaryService, new ScoringService()));
    }

    @Test
    void createGameStartsInLobbyWithOrganiserAsSoleMember() {
        Game game = gameService.createGame("Alice");

        assertThat(game.getId()).hasSize(5);
        assertThat(game.getStatus()).isEqualTo(GameStatus.LOBBY);
        assertThat(game.playerList()).hasSize(1);
        assertThat(game.getPlayer(game.getOrganiserId()).getName()).isEqualTo("Alice");
    }

    @Test
    void secondPlayerCanJoinLobby() {
        Game game = gameService.createGame("Alice");

        Player bob = gameService.joinGame(game.getId(), "Bob");

        assertThat(game.playerList()).hasSize(2);
        assertThat(bob.getName()).isEqualTo("Bob");
    }

    @Test
    void duplicateNameInSameLobbyIsRejected() {
        Game game = gameService.createGame("Alice");

        assertThatThrownBy(() -> gameService.joinGame(game.getId(), "alice"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void cannotJoinAfterGameHasStarted() {
        Game game = gameService.createGame("Alice");
        gameService.startGame(game.getId(), game.getOrganiserId());

        assertThatThrownBy(() -> gameService.joinGame(game.getId(), "Bob"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void onlyOrganiserCanStartTheGame() {
        Game game = gameService.createGame("Alice");
        Player bob = gameService.joinGame(game.getId(), "Bob");

        assertThatThrownBy(() -> gameService.startGame(game.getId(), bob.getId()))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void startingSetsScrambledWordAndEndsAtThirtySecondsLater() {
        Game game = gameService.createGame("Alice");

        Game started = gameService.startGame(game.getId(), game.getOrganiserId());

        assertThat(started.getStatus()).isEqualTo(GameStatus.IN_PROGRESS);
        assertThat(started.getScrambledWord()).hasSize(8);
        assertThat(started.getSolutionWord()).hasSize(8);
        assertThat(started.getEndsAt()).isEqualTo(started.getStartedAt().plus(GameService.ROUND_DURATION));
    }

    @Test
    void cannotStartAGameThatIsAlreadyInProgress() {
        Game game = gameService.createGame("Alice");
        gameService.startGame(game.getId(), game.getOrganiserId());

        assertThatThrownBy(() -> gameService.startGame(game.getId(), game.getOrganiserId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void submitGuessBeforeGameStartsIsRejected() {
        Game game = gameService.createGame("Alice");

        assertThatThrownBy(() -> gameService.submitGuess(game.getId(), game.getOrganiserId(), "cat"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void unknownPlayerCannotSubmitAGuess() {
        Game game = gameService.createGame("Alice");
        gameService.startGame(game.getId(), game.getOrganiserId());

        assertThatThrownBy(() -> gameService.submitGuess(game.getId(), UUID.randomUUID(), "cat"))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void validGuessScoresPointsAndUpdatesSortedPlayerList() {
        Game game = gameService.createGame("Alice");
        game.setScrambledWord("TARDIGEN"); // letters of GRADIENT
        game.setStatus(GameStatus.IN_PROGRESS);

        GuessSubmissionResult result = gameService.submitGuess(game.getId(), game.getOrganiserId(), "rating");

        assertThat(result.evaluation().outcome()).isEqualTo(GuessOutcome.VALID);
        assertThat(result.evaluation().points()).isEqualTo(10);
        assertThat(result.players().get(0).score()).isEqualTo(10);
    }

    @Test
    void duplicateGuessDoesNotScoreTwice() {
        Game game = gameService.createGame("Alice");
        game.setScrambledWord("TARDIGEN");
        game.setStatus(GameStatus.IN_PROGRESS);

        gameService.submitGuess(game.getId(), game.getOrganiserId(), "rating");
        GuessSubmissionResult second = gameService.submitGuess(game.getId(), game.getOrganiserId(), "RATING");

        assertThat(second.evaluation().outcome()).isEqualTo(GuessOutcome.ALREADY_FOUND);
        assertThat(second.players().get(0).score()).isEqualTo(10);
    }

    @Test
    void finalizeGameDeclaresHighestScorerAsSoleWinner() {
        Game game = gameService.createGame("Alice");
        Player bob = gameService.joinGame(game.getId(), "Bob");
        game.setScrambledWord("TARDIGEN");
        game.setSolutionWord("GRADIENT");
        game.setStatus(GameStatus.IN_PROGRESS);

        gameService.submitGuess(game.getId(), game.getOrganiserId(), "rating"); // 10 points
        gameService.submitGuess(game.getId(), bob.getId(), "art"); // 1 point

        GameEndResult end = gameService.finalizeGame(game.getId()).orElseThrow();

        assertThat(end.solutionWord()).isEqualTo("GRADIENT");
        assertThat(end.winners()).hasSize(1);
        assertThat(end.winners().get(0).name()).isEqualTo("Alice");
        assertThat(game.getStatus()).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    void finalizeGameListsAllTiedPlayersAsCoWinners() {
        Game game = gameService.createGame("Alice");
        Player bob = gameService.joinGame(game.getId(), "Bob");
        game.setScrambledWord("TARDIGEN");
        game.setStatus(GameStatus.IN_PROGRESS);

        gameService.submitGuess(game.getId(), game.getOrganiserId(), "rat"); // 1 point
        gameService.submitGuess(game.getId(), bob.getId(), "art"); // 1 point

        GameEndResult end = gameService.finalizeGame(game.getId()).orElseThrow();

        assertThat(end.winners()).hasSize(2);
    }

    @Test
    void finalizeGameBuildsAuditDataForEveryPlayer() {
        Game game = gameService.createGame("Alice", "1.1.1.1");
        Player bob = gameService.joinGame(game.getId(), "Bob", "2.2.2.2");
        game.setScrambledWord("TARDIGEN");
        game.setSolutionWord("GRADIENT");
        game.setStatus(GameStatus.IN_PROGRESS);

        gameService.submitGuess(game.getId(), game.getOrganiserId(), "gradient"); // finds the 8-letter word
        gameService.submitGuess(game.getId(), bob.getId(), "art"); // 1 point, doesn't find it

        GameEndResult end = gameService.finalizeGame(game.getId()).orElseThrow();

        assertThat(end.createdAt()).isEqualTo(game.getCreatedAt());
        PlayerAuditView aliceAudit = end.playerAudits().stream()
                .filter(a -> a.name().equals("Alice")).findFirst().orElseThrow();
        PlayerAuditView bobAudit = end.playerAudits().stream()
                .filter(a -> a.name().equals("Bob")).findFirst().orElseThrow();

        assertThat(aliceAudit.organiser()).isTrue();
        assertThat(aliceAudit.ipAddress()).isEqualTo("1.1.1.1");
        assertThat(aliceAudit.foundEightLetterWord()).isTrue();
        assertThat(aliceAudit.winner()).isTrue();
        assertThat(aliceAudit.score()).isEqualTo(20);

        assertThat(bobAudit.organiser()).isFalse();
        assertThat(bobAudit.ipAddress()).isEqualTo("2.2.2.2");
        assertThat(bobAudit.foundEightLetterWord()).isFalse();
        assertThat(bobAudit.winner()).isFalse();
        assertThat(bobAudit.score()).isEqualTo(1);
    }

    @Test
    void finalizeGameOnlyReturnsAResultTheFirstTimeItsCalled() {
        Game game = gameService.createGame("Alice");
        game.setScrambledWord("TARDIGEN");
        game.setStatus(GameStatus.IN_PROGRESS);

        assertThat(gameService.finalizeGame(game.getId())).isPresent();
        assertThat(gameService.finalizeGame(game.getId())).isEmpty();
    }

    @Test
    void organiserCanQuitAnInProgressGame() {
        Game game = gameService.createGame("Alice");
        game.setStatus(GameStatus.IN_PROGRESS);

        gameService.requestQuit(game.getId(), game.getOrganiserId());
        // requestQuit only validates; GameFinalizerScheduler performs the actual finalization.
        assertThat(game.getStatus()).isEqualTo(GameStatus.IN_PROGRESS);
    }

    @Test
    void nonOrganiserCannotQuitTheGame() {
        Game game = gameService.createGame("Alice");
        Player bob = gameService.joinGame(game.getId(), "Bob");
        game.setStatus(GameStatus.IN_PROGRESS);

        assertThatThrownBy(() -> gameService.requestQuit(game.getId(), bob.getId()))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void cannotQuitAGameThatHasNotStarted() {
        Game game = gameService.createGame("Alice");

        assertThatThrownBy(() -> gameService.requestQuit(game.getId(), game.getOrganiserId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void snapshotIncludesWinnersOnceGameIsFinished() {
        Game game = gameService.createGame("Alice");
        game.setStatus(GameStatus.IN_PROGRESS);

        assertThat(gameService.getSnapshot(game.getId(), null).winners()).isEmpty();

        gameService.finalizeGame(game.getId());

        var snapshot = gameService.getSnapshot(game.getId(), null);
        assertThat(snapshot.winners()).hasSize(1);
        assertThat(snapshot.winners().get(0).name()).isEqualTo("Alice");
    }

    @Test
    void listJoinableGamesReturnsOnlyLobbyGames() {
        Game lobbyGame = gameService.createGame("Alice");
        gameService.joinGame(lobbyGame.getId(), "Bob");
        Game startedGame = gameService.createGame("Carol");
        gameService.startGame(startedGame.getId(), startedGame.getOrganiserId());

        var joinable = gameService.listJoinableGames();

        assertThat(joinable).hasSize(1);
        assertThat(joinable.get(0).gameId()).isEqualTo(lobbyGame.getId());
        assertThat(joinable.get(0).organiserName()).isEqualTo("Alice");
        assertThat(joinable.get(0).playerCount()).isEqualTo(2);
    }

    @Test
    void concurrentJoinsDoNotCorruptPlayerList() throws InterruptedException {
        Game game = gameService.createGame("Alice");
        int joiners = 20;
        ExecutorService pool = Executors.newFixedThreadPool(joiners);
        CountDownLatch ready = new CountDownLatch(joiners);
        CountDownLatch go = new CountDownLatch(1);

        for (int i = 0; i < joiners; i++) {
            int index = i;
            pool.submit(() -> {
                ready.countDown();
                try {
                    go.await();
                    gameService.joinGame(game.getId(), "Player" + index);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
            });
        }
        ready.await();
        go.countDown();
        pool.shutdown();
        pool.awaitTermination(5, TimeUnit.SECONDS);

        assertThat(game.playerList()).hasSize(joiners + 1);
        List<String> names = game.playerList().stream().map(Player::getName).distinct().toList();
        assertThat(names).hasSize(joiners + 1);
    }
}
