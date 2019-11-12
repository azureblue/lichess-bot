package kk.lichess.game;

@FunctionalInterface
public interface GameMoveInterface {
    void sendMove(String move, boolean acceptDraw);
}
