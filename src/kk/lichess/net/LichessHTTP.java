package kk.lichess.net;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import kk.lichess.EventHandler;
import kk.lichess.GameEventHandler;
import kk.lichess.GameStreamParser;
import kk.lichess.Log;
import kk.lichess.net.pojo.Challenge;
import kong.unirest.HttpRequestWithBody;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class LichessHTTP {
    private final String authToken;
    private final ObjectMapper mapper = new JsonMapper();

    public LichessHTTP(String authToken) {
        this.authToken = authToken;
    }

    public LichessResponse get(String path) throws LichessHTTPException {
        try {
            HttpResponse<String> response = Unirest.get(path)
                    .header("Authorization", authToken)
                    .asString();
            return new LichessResponse(response.getStatus(), response.getBody());
        } catch (UnirestException ue) {
            throw new LichessHTTPException(ue);
        }
    }

    public LichessResponse postChatMessage(String gameId, Room room, String message) {
        HttpResponse<String> response = Unirest.post("https://lichess.org/api/bot/game/{gameId}/chat")
                .routeParam("gameId", gameId)
                .header("Authorization", authToken)
                .field("room", room.value)
                .field("text", message)
                .asString();
        return new LichessResponse(response.getStatus(), response.getBody());
    }

    public LichessResponse postMove(String gameId, String move, boolean acceptDraw) throws LichessHTTPException {
        try {
            HttpResponse<String> response = Unirest.post("https://lichess.org/api/bot/game/{gameId}/move/{move}")
                    .routeParam("gameId", gameId)
                    .routeParam("move", move)
                    .socketTimeout(2000)
                    .queryString(acceptDraw ? Map.of("offeringDraw", true) : Map.of())
                    .header("Authorization", authToken)
                    .asString();

            return new LichessResponse(response.getStatus(), response.getBody());
        } catch (UnirestException e) {
            Throwable t = e;
            while (t != null) {
                System.out.println(t.getClass().getName());
                if (t instanceof SocketTimeoutException) {
                    if (acceptDraw) {
                        throw new LichessHTTPException("socket timeout after accepting draw, we're ok :)", null);
                        //TODO contact lichess
                        //lichess should close game after game finishes with draw
                    }
                }
                t = t.getCause();
            }
            throw new LichessHTTPException("post move exception", e);
        }
    }

    public LichessResponse post(String path) throws LichessHTTPException {
        try {
            HttpRequestWithBody postRequest = Unirest.post(path)
                    .socketTimeout(5000)
                    .header("Authorization", authToken);

            HttpResponse<String> response = postRequest.asString();
            return new LichessResponse(response.getStatus(), response.getBody());
        } catch (UnirestException ue) {
            throw new LichessHTTPException(ue);
        }
    }

    private InputStream openStream(String url) throws IOException {
        HttpURLConnection urlConnection =
                (HttpURLConnection) new URL(url).openConnection();
        urlConnection.setRequestMethod("GET");
        urlConnection.setRequestProperty("Authorization", authToken);
        return urlConnection.getInputStream();
    }

    public LichessStream eventStream(BiConsumer<LichessStream, LichessStream.StreamResult> whenComplete,
                                     Consumer<Challenge> challengeHandler, Consumer<String> gameStartHandler) {
        return stream("https://lichess.org/api/stream/event", whenComplete, new EventHandler(challengeHandler, gameStartHandler));
    }

    public LichessStream gameStream(BiConsumer<LichessStream, LichessStream.StreamResult> whenComplete, String gameId,
                                    GameEventHandler gameEventHandler) {
        return stream("https://lichess.org/api/bot/game/stream/" + gameId, whenComplete, new GameStreamParser(gameEventHandler));
    }

    private LichessStream stream(String url, BiConsumer<LichessStream, LichessStream.StreamResult> whenComplete, LichessStream.JsonHandler handler) {
        return new LichessStream(() -> openStream(url), whenComplete, handler);
    }

    public Set<String> gamesInProgress() {
        HashSet<String> res = new HashSet<>();
        get("https://lichess.org/api/account/playing")
                .toJson()
                .get("nowPlaying")
                .elements()
                .forEachRemaining(gameNode -> {
                    res.add(gameNode.get("fullId").asText());
                    res.add(gameNode.get("gameId").asText());
                });
        Log.v("ongoing games: " + res);
        return res;
    }

    public enum Room {
        Player("player"), Spectator("spectator");
        private final String value;

        Room(String value) {
            this.value = value;
        }
    }

    public class LichessResponse {
        private final int statusCode;
        private final String content;

        private LichessResponse(int statusCode, String content) {
            this.statusCode = statusCode;
            this.content = content;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public String getContent() {
            return content;
        }

        public JsonNode toJson() throws LichessHTTPException {
            try {
                return mapper.readTree(content);
            } catch (JsonProcessingException e) {
                throw new LichessHTTPException(e);
            }
        }
    }

}
