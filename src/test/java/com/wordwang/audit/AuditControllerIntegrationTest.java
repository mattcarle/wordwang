package com.wordwang.audit;

import com.wordwang.game.dto.GameEndResult;
import com.wordwang.game.dto.PlayerAuditView;
import com.wordwang.game.dto.PlayerView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Confirms /api/admin/audit is covered by SecurityConfig's existing /api/admin/** gate (no
 * dedicated security wiring of its own) and that pagination/date filtering actually work against
 * real persisted rows. Same raw-HttpClient-with-cookie-jar approach as
 * AdminControllerIntegrationTest, for the same reasons (see that class).
 *
 * <p>Uses its own named in-memory database (distinct from the shared "wordwang-test" one other
 * @SpringBootTest classes use) so this test's admin-setup call doesn't collide with
 * AdminControllerIntegrationTest's - both set up the same literal password, and a shared context
 * would make whichever test runs second see "already set up" instead of a clean slate.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.datasource.url=jdbc:h2:mem:audit-controller-test;DB_CLOSE_DELAY=-1")
class AuditControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private AuditService auditService;

    private final CookieManager cookieManager = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
    private final HttpClient httpClient = HttpClient.newBuilder().cookieHandler(cookieManager).build();

    @BeforeEach
    void primeCsrfCookie() throws Exception {
        get("/api/admin/setup-status");
    }

    private void recordGame(String gameId, Instant createdAt) {
        List<PlayerAuditView> playerAudits = List.of(new PlayerAuditView("Alice", true, 20, true, true, null));
        List<PlayerView> players = List.of(new PlayerView(UUID.randomUUID(), "Alice", 20));
        auditService.recordGame(new GameEndResult(gameId, createdAt, "GRADIENT", players, players, playerAudits));
    }

    @Test
    void auditEndpointRequiresAuthenticationThenReturnsFilteredResults() throws Exception {
        assertThat(get("/api/admin/audit").statusCode()).isEqualTo(401);

        assertThat(postJson("/api/admin/setup", "{\"password\":\"correct-horse-battery\"}").statusCode()).isEqualTo(204);

        Instant now = Instant.now();
        recordGame("55555", now.minus(java.time.Duration.ofDays(5)));
        recordGame("66666", now);

        HttpResponse<String> unfiltered = get("/api/admin/audit?page=0&size=20");
        assertThat(unfiltered.statusCode()).isEqualTo(200);
        assertThat(unfiltered.body()).contains("\"totalElements\":2");
        assertThat(unfiltered.body()).contains("55555").contains("66666");

        String from = now.minus(java.time.Duration.ofDays(1)).toString();
        HttpResponse<String> filtered = get("/api/admin/audit?page=0&size=20&from=" + from);
        assertThat(filtered.statusCode()).isEqualTo(200);
        assertThat(filtered.body()).contains("\"totalElements\":1");
        assertThat(filtered.body()).contains("66666");
        assertThat(filtered.body()).doesNotContain("55555");
    }

    private HttpResponse<String> get(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + path))
                .GET()
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> postJson(String path, String jsonBody) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + path))
                .header("Content-Type", "application/json")
                .header("X-XSRF-TOKEN", csrfToken())
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private String csrfToken() {
        return cookieManager.getCookieStore().getCookies().stream()
                .filter(cookie -> cookie.getName().equals("XSRF-TOKEN"))
                .map(HttpCookie::getValue)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No XSRF-TOKEN cookie in jar"));
    }
}
