package kk.lichess.game;

import kk.lichess.Side;

import java.util.List;

public interface GameHandler {
    void handleGameStart(String initialFen, Side side, int time, int timeBonus);

    void handleDrawOffer();

    void handleGameState(List<String> moves, int timeLeft, GameMoveInterface gameInterface);
}
