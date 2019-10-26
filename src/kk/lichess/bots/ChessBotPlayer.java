package kk.lichess.bots;

import kk.chessbot.Board;
import kk.chessbot.Side;
import kk.chessbot.moves.BoardUtils;
import kk.chessbot.player.NotSoRandomPlayer;
import kk.chessbot.wrappers.Move;
import kk.lichess.ChessPlayer;

public class ChessBotPlayer implements ChessPlayer {

    private NotSoRandomPlayer notSoRandomPlayer = null;
    private final Board temp = new Board();

    @Override
    public void gameStarts(boolean playerIsWhite, int remainingTime) {
        notSoRandomPlayer = new NotSoRandomPlayer(BoardUtils.board().starting().build(), playerIsWhite ? Side.White : Side.Black);
    }

    @Override
    public String makeMove(int remainingTime) {
        Move move = notSoRandomPlayer.makeMove();
        System.out.println("my move: " + move);
        if (move == null) {
            System.out.println("game lost!");
            return null;
        }
        return MoveUtils.lichesMove(move);

    }

    @Override
    public void applyMove(String move) {
        System.out.println("applying move: " + move);
        notSoRandomPlayer.getBoard(temp);
        notSoRandomPlayer.applyMove(MoveUtils.fromLichess(move, temp));
        System.out.println(temp.toUnicodeMultiline());
    }
}
