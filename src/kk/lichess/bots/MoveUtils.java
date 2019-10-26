package kk.lichess.bots;

import kk.chessbot.Board;
import kk.chessbot.Piece;
import kk.chessbot.wrappers.Move;
import kk.chessbot.wrappers.Position;

public class MoveUtils {
    public static String lichesMove(Move move) {
        String moveString = move.toLongNotation().replace("x", "");
        if (!Character.isDigit(moveString.charAt(1)))
            moveString = moveString.substring(1);
        return moveString;
    }

    public static  Move fromLichess(String lichessMove, Board board) {
        Position from = Position.position(lichessMove.substring(0, 2));
        Position to = Position.position(lichessMove.substring(2, 4));
        String promote = "";

        if (lichessMove.length() > 4)
            promote = lichessMove.substring(4, 5).toUpperCase();

        String capture = "";
        if (!board.isEmpty(to.raw()))
            capture = "x";
        Piece piece = board.piece(from.raw());

        return Move.from("" + (piece == Piece.Pawn ? "" : piece.symbol) + from.toString() + capture + to.toString() + promote);
    }

}
