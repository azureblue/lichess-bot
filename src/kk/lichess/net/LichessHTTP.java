package kk.lichess.net;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import kk.lichess.LichessBot;
import kk.lichess.Log;
import kong.unirest.HttpRequestWithBody;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Map;

public class LichessHTTP {
    private static final String authToken;
    private static final ObjectMapper mapper = new JsonMapper();

    static {
        try {
            JsonNode config = new ObjectMapper().readTree(LichessBot.class.getResourceAsStream("/lichess-bot.json"));
            authToken = config.get("authToken").asText();
        } catch (IOException e) {
            throw new IllegalStateException("unable to read auth token");
        }
    }

    public enum Room {
        Player("player"), Spectator("spectator");
        private final String value;

        Room(String value) {
            this.value = value;
        }
    }

    public static class LichessResponse {
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

        public JsonNode toJson() throws JsonProcessingException {
            return mapper.readTree(content);
        }

    }

    public static LichessResponse get(String path) throws LichessHTTPException {
        try {
            HttpResponse<String> response = Unirest.get(path)
                    .header("Authorization", authToken)
                    .asString();
            return new LichessResponse(response.getStatus(), response.getBody());
        } catch (UnirestException ue) {
            throw new LichessHTTPException(ue);
        }
    }


    public static LichessResponse postChatMessage(String gameId, Room room, String message) {
        HttpResponse<String> response = Unirest.post("https://lichess.org/api/bot/game/{gameId}/chat")
                .routeParam("gameId", gameId)
                .header("Authorization", authToken)
                .field("room", room.value)
                .field("text", message)
                .asString();
        return new LichessResponse(response.getStatus(), response.getBody());
    }

    public static LichessResponse postMove(String gameId, String move, boolean acceptDraw) throws LichessHTTPException {
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

    public static LichessResponse post(String path) throws LichessHTTPException {
        try {
            HttpRequestWithBody postRequest = Unirest.post(path)
                    .header("Authorization", authToken);

            HttpResponse<String> response = postRequest.asString();
            return new LichessResponse(response.getStatus(), response.getBody());
        } catch (UnirestException ue) {
            throw new LichessHTTPException(ue);
        }
    }

    private static InputStream openStream(String url) throws IOException {
        HttpURLConnection urlConnection =
                (HttpURLConnection) new URL(url).openConnection();
        urlConnection.setRequestMethod("GET");
        urlConnection.setRequestProperty("Authorization", authToken);
        return urlConnection.getInputStream();
    }

    public static InputStreamSupplier stream(String path) {
        return () -> openStream(path);
    }

    public static InputStreamSupplier gameStream(String gameId) {
        return () -> openStream("https://lichess.org/api/bot/game/stream/" + gameId);
    }
}
