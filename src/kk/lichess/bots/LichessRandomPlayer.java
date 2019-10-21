package kk.lichess.bots;

import kk.chessbot.Board;
import kk.chessbot.RandomPlayer;
import kk.chessbot.moves.BoardUtils;
import kk.chessbot.wrappers.Move;
import kk.lichess.ChessPlayer;

public class LichessRandomPlayer implements ChessPlayer {

    Board board = BoardUtils.board().starting().build();

    RandomPlayer randomPlayer = null;

    @Override
    public void gameStarts(boolean playerIsWhite, int remainingTime) {
        randomPlayer = new RandomPlayer(board, playerIsWhite);
    }

    @Override
    public String move(int remainingTime) {
        Move move = randomPlayer.move();
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
        board.apply(MoveUtils.fromLichess(move, board));
        System.out.println(board.toUnicodeMultiline());
    }
}
