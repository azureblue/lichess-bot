package kk.lichess;

import kk.lichess.api.ChatLine;
import kk.lichess.api.GameFull;
import kk.lichess.api.GameState;
import kong.unirest.HttpRequestWithBody;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import kong.unirest.json.JSONObject;

import java.net.SocketTimeoutException;

public class GameHandler implements GameEventHandler {
    private final String authToken;
    private String gameId;
    private final String playerId;
    private final ChessPlayer chessPlayer;
    private boolean playingWhite = false;
    int moveCount = 0;
    private boolean acceptDraw;

    public GameHandler(String authToken, String playerId, ChessPlayer chessPlayer) {
        this.authToken = authToken;
        this.playerId = playerId;
        this.chessPlayer = chessPlayer;
    }

    @Override
    public void handleGameFull(GameFull gameFull) {
        gameId = gameFull.getId();
        System.out.println("handling game: " + gameId);
        if (gameFull.getWhite().getId().equals(playerId)) {
            this.playingWhite = true;
            chessPlayer.gameStarts(true, gameFull.getClock().getInitial());
        } else if (gameFull.getBlack().getId().equals(playerId)) {
            chessPlayer.gameStarts(false, gameFull.getClock().getInitial());
        } else
            throw new IllegalStateException("bot player id doesn't match");


        String moves = gameFull.getGameState().getMoves();
        if (!moves.trim().equals("")) {
            for (String move : moves.split(" ")) {
                if (move.trim().equals(""))
                    continue;
                moveCount++;
                chessPlayer.applyMove(move);
            }
        }

        handleGameState(gameFull.getGameState());

    }

    @Override
    public void handleGameState(GameState gameState) {
        String moves = gameState.getMoves();
        System.out.println("handling new state: " + moves);
        String[] moveArray = moves.split(" ");
        if (!moves.trim().equals("")) {
            while (moveCount < moveArray.length) {
                chessPlayer.applyMove(moveArray[moveCount++]);
            }
        } else
            moveArray = new String[0];

        boolean myMove = ((moveArray.length % 2 == 0) == playingWhite);
        if (myMove) {
            System.out.println("my turn");
            String move = chessPlayer.move(0);
            if (move == null)
                return;
            sendMove(move);
        } else {
            System.out.println("opoonent turn");
        }
    }

    private void sendMove(String move) {
        HttpRequestWithBody request = Unirest.post("https://lichess.org/api/bot/game/{gameId}/move/{move}")
                .routeParam("gameId", gameId)
                .routeParam("move", move);

        if (acceptDraw) {
            request.socketTimeout(2000);
            try {
                request.queryString("offeringDraw", "true").header("Authorization", authToken).asEmpty();
            } catch (UnirestException e) {
                if (e.getCause() instanceof SocketTimeoutException) {
                    System.out.println("connection timeout");
                } else throw e;
            }
            acceptDraw = false;
            return;
        }
        JsonNode response = request
                .header("Authorization", authToken)
                .asJson()
                .getBody();
        if (response == null && acceptDraw) {
            return;
        }
        JSONObject responseObject = response.getObject();

        if (!Boolean.parseBoolean(responseObject.optString("ok", "false"))) {
            throw new IllegalStateException("lichess move error: " + responseObject.get("error"));
        }
    }

    @Override
    public void handleChatLine(ChatLine chatLine) {
        if (chatLine.getUsername().equals("lichess")
                && chatLine.getText().toLowerCase().equals("" + (playingWhite ? "black" : "white") + " offers draw")) {
            this.acceptDraw = true;
        }
    }
}
