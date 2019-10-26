package kk.lichess;

public interface ChessPlayer {
    void gameStarts(boolean playerIsWhite, int remainingTime);

    String makeMove(int remainingTime);

    void applyMove(String move);
}
