package kk.lichess;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import kk.lichess.api.Challenge;
import kk.lichess.net.LichessStream.JsonHandler;

import java.io.IOException;
import java.util.function.Consumer;

public class EventHandler implements JsonHandler {

    private JsonMapper mapper = new JsonMapper();

    private final Consumer<Challenge> handler;
    private final Consumer<String> gameStartHandler;

    public EventHandler(Consumer<Challenge> handler, Consumer<String> gameStartHandler) {
        this.handler = handler;
        this.gameStartHandler = gameStartHandler;
    }

    @Override
    public void handleJson(String json) {
        try {
            JsonNode eventJson = mapper.readTree(json);
            String type = eventJson.get("type").asText();
            if (type.equals("challenge")) {
                Challenge challenge = mapper.treeToValue(eventJson.get("challenge"), Challenge.class);
                Log.i("challenge event: " + challenge.getId() + " | " + challenge.getChallenger().getId());
                handler.accept(challenge);
            }
            else if (type.equals("gameStart")) {
                String gameId = eventJson.get("game").get("id").asText();
                Log.i("game start event:" + gameId);
                gameStartHandler.accept(gameId);
            }
            else {
                throw new IllegalStateException("unsupported event: " + type);
            }
        } catch (IOException e) {
            Log.e(e.getMessage() + ": " + json);
        }
    }
}
