package kk.lichess;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import kk.lichess.net.pojo.ChatLine;
import kk.lichess.net.pojo.GameFull;
import kk.lichess.net.pojo.GameState;
import kk.lichess.net.LichessStream;

import java.io.IOException;

public class GameStreamParser implements LichessStream.JsonHandler {
    private JsonMapper mapper = new JsonMapper();
    private final GameEventHandler gameEventHandler;

    public GameStreamParser(GameEventHandler gameEventHandler) {
        this.gameEventHandler = gameEventHandler;
    }

    @Override
    public void handleJson(String json) {
        try {
            JsonNode element = mapper.readTree(json);
            String type = element.get("type").asText();
            switch (type) {
                case "gameFull":
                    GameFull gameFull = mapper.treeToValue(element, GameFull.class);
                    gameEventHandler.handleGameFull(gameFull);
                    break;
                case "gameState":
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
