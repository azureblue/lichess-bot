package kk.lichess;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import kk.lichess.api.ChatLine;
import kk.lichess.api.GameFull;
import kk.lichess.api.GameState;

import java.io.IOException;
import java.util.function.Consumer;

public class GameStreamHandler implements Consumer<String> {
    private JsonMapper mapper = new JsonMapper();
    private final GameEventHandler gameEventHandler;

    public GameStreamHandler(GameEventHandler gameEventHandler) {
        this.gameEventHandler = gameEventHandler;
    }

    @Override
    public void accept(String json) {
        try {
            JsonNode element = mapper.readTree(json);
            String type = element.get("type").asText();
            switch (type) {
                case "gameFull":
                    GameFull gameFull = mapper.treeToValue(element, GameFull.class);
                    gameEventHandler.handleGameFull(gameFull);
                    break;
                case "gameState":
                    System.out.println(element);
                    GameState gameState = mapper.treeToValue(element, GameState.class);
                    gameEventHandler.handleGameState(gameState);
                    break;
                case "chatLine":
                    ChatLine chatLine = mapper.treeToValue(element, ChatLine.class);
                    gameEventHandler.handleChatLine(chatLine);
                    break;
            }
        } catch (IOException e) {
            System.out.println("malformed json: " + json);
        }
    }

}
