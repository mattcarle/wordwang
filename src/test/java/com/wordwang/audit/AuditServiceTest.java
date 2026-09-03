package com.wordwang.audit;

import com.wordwang.game.dto.GameEndResult;
import com.wordwang.game.dto.PlayerAuditView;
import com.wordwang.game.dto.PlayerView;
import com.wordwang.geolocation.GeolocationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class AuditServiceTest {

    @Autowired
    private GameAuditRepository repository;

    // GeolocationService is real but only ever given loopback/null IPs below, which it resolves
    // to "Unknown" without any network call - see GeolocationServiceTest for that guarantee.
    private AuditService auditService;

    @BeforeEach
    void setUp() {
        auditService = new AuditService(repository, new GeolocationService());
    }

    private GameEndResult resultFor(String gameId, Instant createdAt) {
        List<PlayerAuditView> playerAudits = List.of(
                new PlayerAuditView("Alice", true, 20, true, true, "127.0.0.1"),
                new PlayerAuditView("Bob", false, 5, false, false, null));
        List<PlayerView> players = List.of(
                new PlayerView(UUID.randomUUID(), "Alice", 20),
                new PlayerView(UUID.randomUUID(), "Bob", 5));
        return new GameEndResult(gameId, createdAt, "GRADIENT", players.subList(0, 1), players, playerAudits);
    }

    @Test
    void recordGamePersistsGameAndPlayerRows() {
        auditService.recordGame(resultFor("11111", Instant.now()));

        List<GameAudit> saved = repository.findAll();
        assertThat(saved).hasSize(1);
        GameAudit audit = saved.get(0);
        assertThat(audit.getGameCode()).isEqualTo("11111");
        assertThat(audit.getSolutionWord()).isEqualTo("GRADIENT");
        assertThat(audit.getPlayers()).hasSize(2);

        PlayerAudit alice = audit.getPlayers().stream().filter(p -> p.getPlayerName().equals("Alice")).findFirst().orElseThrow();
        assertThat(alice.isOrganiser()).isTrue();
        assertThat(alice.getScore()).isEqualTo(20);
        assertThat(alice.isFoundEightLetterWord()).isTrue();
        assertThat(alice.isWinner()).isTrue();
        assertThat(alice.getIpAddress()).isEqualTo("127.0.0.1");
        assertThat(alice.getLocation()).isEqualTo("Unknown");

        PlayerAudit bob = audit.getPlayers().stream().filter(p -> p.getPlayerName().equals("Bob")).findFirst().orElseThrow();
        assertThat(bob.getIpAddress()).isNull();
        assertThat(bob.getLocation()).isEqualTo("Unknown");
    }

    @Test
    void searchFiltersByCreatedAtRangeAndPaginates() {
        Instant now = Instant.now();
        auditService.recordGame(resultFor("11111", now.minus(10, ChronoUnit.DAYS)));
        auditService.recordGame(resultFor("22222", now.minus(1, ChronoUnit.DAYS)));
        auditService.recordGame(resultFor("33333", now));

        AuditPageResponse all = auditService.search(null, null, 0, 20);
        assertThat(all.totalElements()).isEqualTo(3);
        assertThat(all.games().get(0).gameCode()).isEqualTo("33333"); // newest first

        AuditPageResponse rangeFiltered = auditService.search(
                now.minus(2, ChronoUnit.DAYS), now.plus(1, ChronoUnit.DAYS), 0, 20);
        assertThat(rangeFiltered.totalElements()).isEqualTo(2);
        assertThat(rangeFiltered.games()).extracting(AuditGameView::gameCode).containsExactly("33333", "22222");

        AuditPageResponse firstPage = auditService.search(null, null, 0, 2);
        assertThat(firstPage.games()).hasSize(2);
        assertThat(firstPage.totalPages()).isEqualTo(2);

        AuditPageResponse secondPage = auditService.search(null, null, 1, 2);
        assertThat(secondPage.games()).hasSize(1);
    }

    @Test
    void auditGameViewIncludesFullPlayerDetail() {
        auditService.recordGame(resultFor("44444", Instant.now()));

        AuditGameView view = auditService.search(null, null, 0, 20).games().get(0);

        assertThat(view.players()).hasSize(2);
        AuditPlayerView alice = view.players().stream().filter(p -> p.name().equals("Alice")).findFirst().orElseThrow();
        assertThat(alice.organiser()).isTrue();
        assertThat(alice.winner()).isTrue();
        assertThat(alice.foundEightLetterWord()).isTrue();
        assertThat(alice.score()).isEqualTo(20);
    }
}
