package kk.lichess;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import kk.lichess.api.Challenge;

import java.io.IOException;
import java.util.function.Consumer;

public class EventStreamHandler implements Consumer<String> {

    private JsonMapper mapper = new JsonMapper();

    private final Consumer<Challenge> handler;
    private final Consumer<String> gameStartHandler;

    public EventStreamHandler(Consumer<Challenge> handler, Consumer<String> gameStartHandler) {
        this.handler = handler;
        this.gameStartHandler = gameStartHandler;
    }

    @Override
    public void accept(String json) {
        try {
            JsonNode eventJson = mapper.readTree(json);
            String type = eventJson.get("type").asText();
            if (type.equals("challenge"))
                handler.accept(mapper.treeToValue(eventJson.get("challenge"), Challenge.class));
            else if (type.equals("gameStart"))
                gameStartHandler.accept(eventJson.get("game").get("id").asText());
            else {
                throw new IllegalStateException("unsupported event: " + type);
            }
        } catch (IOException e) {
            System.out.println("malformed json: " + json);
        }
    }
}
