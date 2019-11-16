package kk.lichess;

import com.fasterxml.jackson.databind.JsonNode;
import kk.lichess.bots.api.ChessPlayer;
import kk.lichess.net.LichessHTTP;
import kk.lichess.net.LichessHTTPException;
import kk.lichess.net.LichessStream;
import kk.lichess.net.pojo.Challenge;

import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class LichessBot {

    private final String botId;
    private final Supplier<ChessPlayer> chessPlayerSupplier;
    private final Predicate<GameRequest> acceptGamePredicate;
    private final LichessHTTP lichessHTTP;
    private LichessGames lichessGames;

    public LichessBot(String botId, String authToken, Supplier<ChessPlayer> chessPlayerSupplier, Predicate<GameRequest> acceptGamePredicate) {
        this.botId = botId;
        this.chessPlayerSupplier = chessPlayerSupplier;
        this.acceptGamePredicate = acceptGamePredicate;
        this.lichessHTTP = new LichessHTTP(authToken);
    }

    private void init() {
        JsonNode user = lichessHTTP.get("https://lichess.org/api/account").toJson();
        if (user.has("error"))
            throw new IllegalStateException("error: " + user.get("error"));

    }

    private GameRequest gameRequestFromChallenge(Challenge challenge) {
        GameRequest.Side side;
        if (challenge.getColor().equals("white"))
            side = GameRequest.Side.White;
        else if (challenge.getColor().equals("black"))
            side = GameRequest.Side.Black;
        else if (challenge.getColor().equals("random"))
            side = GameRequest.Side.Random;
        else
            throw new IllegalArgumentException("invalid side: " + challenge.getColor());


        return new GameRequest(
                challenge.getChallenger().getId(),
                challenge.getChallenger().getRating(),
                challenge.getTimeControl().getLimit(),
                challenge.getTimeControl().getIncrement(),
                side,
                challenge.isRated()
        );
    }

    public void start() {
        new Thread(() -> {
            init();

            lichessGames = new LichessGames(lichessHTTP, botId, this.chessPlayerSupplier);

            Consumer<Challenge> challengeHandler = challenge -> {
                GameRequest gameRequest = gameRequestFromChallenge(challenge);

                boolean challengeAccepted = this.acceptGamePredicate.and(ignore -> lichessGames.size() < 4).test(gameRequest)
                        && (challenge.getVariant().getKey().equals("standard") || challenge.getVariant().getShortName().equals("FEN"));

                String challengeAcceptString = challengeAccepted ? "accept" : "decline";
                LichessHTTP.LichessResponse response = lichessHTTP.post("https://lichess.org/api/challenge/"
                        + challenge.getId() + "/"
                        + challengeAcceptString);

                Log.d("challenge from " + challenge.getChallenger().getId() + ": " + challengeAcceptString);

                if (response.getStatusCode() != 200) {
                    Log.e("lichess error: error while answering challenge: " + response.getContent());
                }
            };

            Consumer<String> gameStartHandler = game -> {
                Log.i("starting game: " + game);
                lichessGames.startGame(game);

            };

            try {
                while (true) {
                    LichessStream eventStream = lichessHTTP.eventStream(
                            (stre, result) -> Log.i("event stream ended: " + result.getResultStatus()),
                            challengeHandler, gameStartHandler);
                    try {
                        Log.i("opening event stream");
                        eventStream.start();
                        eventStream.sync();
                    } catch (IOException | LichessHTTPException e) {
                        Log.e("event stream error", e);
                    } catch (InterruptedException e) {
                        Log.d("lichess event thread interrupted");
                        eventStream.stop();
                        break;
                    }
                    Thread.sleep(2000);
                }
            } catch (InterruptedException e) {
                Log.d("lichess thread interrupted");
            }

        }).start();
    }

    public void stopAll() {
        lichessGames.stopAll();
    }

    public int getNumberOfGamesInProgress() {
        return lichessGames.size();
    }
}
