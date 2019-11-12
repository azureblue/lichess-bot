package kk.lichess.bots;

import kk.lichess.ChessPlayer;
import kk.lichess.Log;

public class ChessBotVerbosePlayer implements ChessPlayer {
    private final ChessPlayer chessPlayer;
    private final String gameId;

    @Override
    public void gameStarts(String fen, boolean playerIsWhite, int remainingTime) {
        chessPlayer.gameStarts(fen, playerIsWhite, remainingTime);
    }

    @Override
    public String makeMove(int remainingTime) {
        String move = chessPlayer.makeMove(remainingTime);
        Log.v("game " + gameId + ": " + "making move " + move);
        return move;
    }

    @Override
    public void applyMove(String move) {
        Log.v("game " + gameId + ": " + "applying move " + move);
        chessPlayer.applyMove(move);
    }

    public ChessBotVerbosePlayer(ChessPlayer chessPlayer, String gameId) {
        this.chessPlayer = chessPlayer;
        this.gameId = gameId;
    }
}
