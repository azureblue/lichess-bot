package kk.lichess.game;

import kk.lichess.bots.api.ChessPlayer;
import kk.lichess.Log;
import kk.lichess.Side;

import java.util.List;

public class ChessPlayerGameHandler implements GameHandler {

    private static final String STARTING_POSITION_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
    private final ChessPlayer chessPlayer;
    private final String gameId;
    private Side me;
    private Side starting;
    private int moveCount = 0;
    private boolean acceptDraw;

    public ChessPlayerGameHandler(ChessPlayer chessPlayer, String gameId) {
        this.chessPlayer = chessPlayer;
        this.gameId = gameId;
    }

    @Override
    public void handleGameStart(String initialFen, Side side, int time, int timeBonus) {
        Log.d(this.getClass().getName(), "handleGameStart: " + initialFen + ", " + side +
                ", " + time + ", " + timeBonus);

        if (initialFen == null || initialFen.equals("startpos"))
            initialFen = STARTING_POSITION_FEN;

        starting = getStartingSide(initialFen);
        me = side;

        chessPlayer.gameStarts(initialFen, me.isWhite(), time);
    }

    @Override
    public void handleGameState(List<String> moves, int timeLeft, GameMoveInterface gameInterface) {
        Log.vv("game " + gameId + " moves: " + moves);

        if (moves.size() < moveCount) {
            Log.d("game " + gameId + " ignoring stale state: " + moves);
            return;
        }

        for (int i = moveCount; i < moves.size(); i++)
            chessPlayer.applyMove(moves.get(i));

        moveCount = moves.size();

        if (isMyMove()) {
            Log.v("game " + gameId + ": " + "my turn");
            String move = chessPlayer.makeMove(timeLeft);
            if (move == null) {
                Log.e("makeMove returned null");
                throw new IllegalStateException("makeMove returned null");
            }
            gameInterface.sendMove(move, acceptDraw);
        } else {
            Log.v("game " + gameId + ": " + "waiting for the opponent's move");
        }
    }

    @Override
    public void handleDrawOffer() {
        acceptDraw = true;
    }

    private boolean isMyMove() {
        Side current = (moveCount % 2) == 0 ? starting : starting.other();
        return current == me;
    }

    private Side getStartingSide(String fen) {
        char startingColor = fen.charAt(fen.indexOf(' ') + 1);

        if (startingColor == 'w')
            return Side.White;
        else if (startingColor == 'b')
            return Side.Black;
        else
            throw new IllegalStateException("invalid fen: " + fen);
    }
}
