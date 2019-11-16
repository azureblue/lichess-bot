package kk.lichess;

import kk.lichess.net.pojo.ChatLine;
import kk.lichess.net.pojo.GameFull;
import kk.lichess.net.pojo.GameState;

public interface GameEventHandler {
    void handleGameFull(GameFull gameFull);

    void handleGameState(GameState gameState);

    void handleChatLine(ChatLine chatLine);
}
