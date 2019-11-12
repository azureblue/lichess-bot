package kk.lichess;

public interface ChessPlayer {
    void gameStarts(String initialFen, boolean playerIsWhite, int remainingTime);

    String makeMove(int remainingTime);

    void applyMove(String move);
}
