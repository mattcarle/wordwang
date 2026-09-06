package com.wordwang.game;

import com.wordwang.dictionary.DictionaryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end check that REST lifecycle calls (create/join/start) and a STOMP
 * guess submission interact correctly against the real in-memory GameService.
 * Avoids TestRestTemplate (needs the spring-boot-restclient module, not a
 * dependency here) and any Jackson-2-based STOMP message converter (this app
 * only has Jackson 3 on its classpath) — REST bodies and STOMP frames are
 * built/parsed as raw JSON strings instead.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GameStompIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private DictionaryService dictionaryService;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private WebSocketStompClient stompClient;

    @BeforeEach
    void setUp() {
        stompClient = new WebSocketStompClient(new SockJsClient(
                List.of(new WebSocketTransport(new StandardWebSocketClient()))));
    }

    @Test
    void guessSubmittedOverStompProducesFeedbackAndScoreUpdate() throws Exception {
        String createBody = postJson("/api/games", "{\"organiserName\":\"Alice\"}");
        String gameId = extractString(createBody, "gameId");
        String organiserId = extractString(createBody, "organiserId");
        assertThat(gameId).hasSize(5);

        postJson("/api/games/" + gameId + "/join", "{\"playerName\":\"Bob\"}");

        BlockingQueue<String> frames = new LinkedBlockingQueue<>();

        StompSession session = stompClient
                .connectAsync("ws://localhost:" + port + "/ws", new StompSessionHandlerAdapter() {
                })
                .get(5, TimeUnit.SECONDS);

        session.subscribe("/topic/game/" + gameId, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return byte[].class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                frames.add(new String((byte[]) payload, StandardCharsets.UTF_8));
            }
        });

        String startBody = postJson("/api/games/" + gameId + "/start", "{\"playerId\":\"" + organiserId + "\"}");
        assertThat(extractString(startBody, "status")).isEqualTo("STARTING");
        assertThat(startBody).contains("countdownEndsAt");

        String startingFrame = frames.poll(5, TimeUnit.SECONDS);
        assertThat(startingFrame).isNotNull();
        assertThat(extractString(startingFrame, "type")).isEqualTo("GAME_STARTING");

        // The organiser-facing countdown plays out over a few real seconds before the round opens.
        String startedFrame = frames.poll(10, TimeUnit.SECONDS);
        assertThat(startedFrame).isNotNull();
        assertThat(extractString(startedFrame, "type")).isEqualTo("GAME_STARTED");
        String scrambledWord = extractString(startedFrame, "scrambledWord");
        assertThat(scrambledWord).hasSize(8);

        String guessWord = findValidThreeLetterWord(scrambledWord);

        StompHeaders sendHeaders = new StompHeaders();
        sendHeaders.setDestination("/app/game/" + gameId + "/guess");
        sendHeaders.setContentType(MimeTypeUtils.APPLICATION_JSON);
        String guessJson = "{\"playerId\":\"" + organiserId + "\",\"word\":\"" + guessWord + "\"}";
        session.send(sendHeaders, guessJson.getBytes(StandardCharsets.UTF_8));

        String feedback = frames.poll(5, TimeUnit.SECONDS);
        String scoreUpdate = frames.poll(5, TimeUnit.SECONDS);

        assertThat(feedback).isNotNull();
        assertThat(extractString(feedback, "type")).isEqualTo("GUESS_FEEDBACK");
        assertThat(extractString(feedback, "outcome")).isEqualTo("VALID");
        assertThat(extractInt(feedback, "points")).isEqualTo(1);

        assertThat(scoreUpdate).isNotNull();
        assertThat(extractString(scoreUpdate, "type")).isEqualTo("SCORE_UPDATE");
    }

    /**
     * Finds a real 3-letter dictionary word that can be spelled using only the
     * letters of {@code scrambledWord} (each letter used at most as many times
     * as it appears), so the test doesn't depend on which random word the
     * server picked.
     */
    private String findValidThreeLetterWord(String scrambledWord) {
        char[] letters = scrambledWord.toCharArray();
        int n = letters.length;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (j == i) continue;
                for (int k = 0; k < n; k++) {
                    if (k == i || k == j) continue;
                    String candidate = "" + letters[i] + letters[j] + letters[k];
                    if (dictionaryService.isValidWord(candidate)) {
                        return candidate;
                    }
                }
            }
        }
        throw new IllegalStateException("No 3-letter word found in scrambled word " + scrambledWord);
    }

    private String postJson(String path, String jsonBody) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode()).isBetween(200, 299);
        return response.body();
    }

    private String extractString(String json, String field) {
        Matcher matcher = Pattern.compile("\"" + field + "\"\\s*:\\s*\"([^\"]*)\"").matcher(json);
        if (!matcher.find()) {
            throw new IllegalStateException("String field " + field + " not found in " + json);
        }
        return matcher.group(1);
    }

    private int extractInt(String json, String field) {
        Matcher matcher = Pattern.compile("\"" + field + "\"\\s*:\\s*(-?\\d+)").matcher(json);
        if (!matcher.find()) {
            throw new IllegalStateException("Numeric field " + field + " not found in " + json);
        }
        return Integer.parseInt(matcher.group(1));
    }
}
