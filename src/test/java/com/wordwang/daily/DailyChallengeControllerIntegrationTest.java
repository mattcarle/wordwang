package com.wordwang.daily;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.datasource.url=jdbc:h2:mem:daily-controller-test;DB_CLOSE_DELAY=-1")
class DailyChallengeControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private DailyChallengeRepository repository;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Test
    void todayReturnsTheCurrentUtcDate() throws Exception {
        HttpResponse<String> response = get("/api/daily");

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains(LocalDate.now(ZoneOffset.UTC).toString());
    }

    @Test
    void todaysLeaderboardNeverIncludesTheSolutionWord() throws Exception {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        repository.save(new DailyChallengeEntry(today, "GRADIENT", "Alice", 25, 100, Instant.now()));

        HttpResponse<String> response = get("/api/daily/leaderboard");

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("Alice").contains("\"percentOfMaxPossible\":25");
        assertThat(response.body()).doesNotContain("GRADIENT");
    }

    @Test
    void historyListsPastDaysWithTheirWordButExcludesToday() throws Exception {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        LocalDate yesterday = today.minusDays(1);
        repository.save(new DailyChallengeEntry(today, "GRADIENT", "Alice", 25, 100, Instant.now()));
        repository.save(new DailyChallengeEntry(yesterday, "RAWHIDED", "Bob", 40, 100, Instant.now()));

        HttpResponse<String> response = get("/api/daily/history");

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains(yesterday.toString()).contains("RAWHIDED");
        assertThat(response.body()).doesNotContain("GRADIENT");
    }

    @Test
    void leaderboardCanBeQueriedForASpecificPastDate() throws Exception {
        LocalDate yesterday = LocalDate.now(ZoneOffset.UTC).minusDays(1);
        repository.save(new DailyChallengeEntry(yesterday, "RAWHIDED", "Bob", 40, 100, Instant.now()));

        HttpResponse<String> response = get("/api/daily/leaderboard?date=" + yesterday);

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("Bob").contains("\"percentOfMaxPossible\":40");
    }

    @Test
    void leaderboardPagesAndCanShowEachPlayerOnlyOnce() throws Exception {
        LocalDate date = LocalDate.now(ZoneOffset.UTC).minusDays(2);
        repository.save(new DailyChallengeEntry(date, "RAWHIDED", "Carol", 60, 100, Instant.now()));
        repository.save(new DailyChallengeEntry(date, "RAWHIDED", "Carol", 30, 100, Instant.now()));
        repository.save(new DailyChallengeEntry(date, "RAWHIDED", "Dave", 50, 100, Instant.now()));

        HttpResponse<String> firstPage = get("/api/daily/leaderboard?date=" + date + "&limit=2");
        HttpResponse<String> unique = get("/api/daily/leaderboard?date=" + date + "&uniquePlayers=true");

        assertThat(firstPage.statusCode()).isEqualTo(200);
        assertThat(firstPage.body()).contains("\"hasMore\":true").contains("Carol").contains("Dave")
                .doesNotContain("\"score\":30");
        assertThat(unique.body()).contains("\"hasMore\":false").contains("\"score\":60").doesNotContain("\"score\":30");
    }

    @Test
    void leaderboardRejectsAnUnknownSort() throws Exception {
        assertThat(get("/api/daily/leaderboard?sort=nonsense").statusCode()).isEqualTo(400);
    }

    private HttpResponse<String> get(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + path))
                .GET()
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
