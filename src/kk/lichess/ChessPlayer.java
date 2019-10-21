package kk.lichess;

public interface ChessPlayer {
    void gameStarts(boolean playerIsWhite, int remainingTime);

    String move(int remainingTime);

    void applyMove(String move);
}
