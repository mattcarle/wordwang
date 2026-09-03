package com.wordwang.admin;

import com.wordwang.highscore.HighScoreService;
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

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Exercises the real HTTP + cookie stack (session, CSRF double-submit, path scoping) end to end -
 * see SecurityConfig. Uses a raw java.net.http.HttpClient with a real cookie jar rather than
 * MockMvc/TestRestTemplate, same reasoning as GameStompIntegrationTest.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AdminControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private HighScoreService highScoreService;

    private final CookieManager cookieManager = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
    private final HttpClient httpClient = HttpClient.newBuilder().cookieHandler(cookieManager).build();

    @BeforeEach
    void primeCsrfCookie() throws Exception {
        // Every request runs the CsrfCookieFilter, so a plain GET already deposits the
        // XSRF-TOKEN cookie the admin page would read before ever making a state-changing call.
        get("/api/admin/setup-status");
    }

    @Test
    void setupThenLoginLifecycleGatesTheClearHighScoresAction() throws Exception {
        assertThat(get("/api/admin/setup-status").body()).contains("\"setupRequired\":true");
        assertThat(delete("/api/admin/highscores").statusCode()).isEqualTo(401);

        assertThat(postJson("/api/admin/setup", "{\"password\":\"short\"}").statusCode()).isEqualTo(400);
        assertThat(get("/api/admin/setup-status").body()).contains("\"setupRequired\":true");

        assertThat(postJson("/api/admin/setup", "{\"password\":\"correct-horse-battery\"}").statusCode()).isEqualTo(204);
        assertThat(get("/api/admin/setup-status").body()).contains("\"setupRequired\":false");
        assertThat(get("/api/admin/me").statusCode()).isEqualTo(204);

        // Setup already logged us in - prove the admin action actually clears real data, not just 204s.
        highScoreService.recordScore("Alice", 20, "12345");
        assertThat(highScoreService.getTopScores(20)).isNotEmpty();
        assertThat(delete("/api/admin/highscores").statusCode()).isEqualTo(204);
        assertThat(highScoreService.getTopScores(20)).isEmpty();

        assertThat(postJson("/api/admin/setup", "{\"password\":\"another-one\"}").statusCode()).isEqualTo(409);

        assertThat(postJson("/api/admin/logout", "").statusCode()).isEqualTo(204);
        assertThat(get("/api/admin/me").statusCode()).isEqualTo(401);
        assertThat(delete("/api/admin/highscores").statusCode()).isEqualTo(401);

        assertThat(postJson("/api/admin/login", "{\"password\":\"wrong\"}").statusCode()).isEqualTo(401);
        assertThat(get("/api/admin/me").statusCode()).isEqualTo(401);

        assertThat(postJson("/api/admin/login", "{\"password\":\"correct-horse-battery\"}").statusCode()).isEqualTo(204);
        assertThat(get("/api/admin/me").statusCode()).isEqualTo(204);
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

    private HttpResponse<String> delete(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + path))
                .header("X-XSRF-TOKEN", csrfToken())
                .DELETE()
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
