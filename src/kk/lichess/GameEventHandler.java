package kk.lichess;

import kk.lichess.api.ChatLine;
import kk.lichess.api.GameFull;
import kk.lichess.api.GameState;

public interface GameEventHandler {
    void handleGameFull(GameFull gameFull);

    void handleGameState(GameState gameState);

    void handleChatLine(ChatLine chatLine);
}
