package kk.lichess;

import kk.lichess.api.Challenge;
import kk.lichess.api.Side;
import kk.lichess.bots.LichessRandomPlayer;
import kk.lichess.net.JsonStream;
import kk.lichess.net.LichessHTTP;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class LichessBot {
    private final LichessHTTP lichessHTTP;

    public LichessBot(String authToken) {
        lichessHTTP = new LichessHTTP(authToken);
    }


    public void init() {
        System.out.println(lichessHTTP.get("/api/account").toString());
    }

    private GameRequest gameRequestFromChallenge(Challenge challenge) {
        return new GameRequest(
                challenge.getChallenger().getId(),
                challenge.getChallenger().getRating(),
                challenge.getTimeControl().getLimit(),
                challenge.getTimeControl().getIncrement(),
                Side.fromChar(challenge.getColor().charAt(0)),
                challenge.isRated()
        );
    }

    public void start(Predicate<GameRequest> acceptGamePredicate, Supplier<GameEventHandler> gameEventHandler) throws IOException {
        EventStreamHandler eventStreamHandler = new EventStreamHandler(
                challenge -> {
                    GameRequest gameRequest = gameRequestFromChallenge(challenge);

                    String challengeAcceptResult = lichessHTTP.post("api/challenge/"
                            + challenge.getId() + "/"
                            + (acceptGamePredicate.test(gameRequest) ? "accept" : "decline")).toString();

                    System.out.println(challengeAcceptResult);

                    if (!challengeAcceptResult.contains("error")) {
                        System.out.println("lichess error: error while answering challenge: " + challengeAcceptResult);
                    }
                },
                game -> {
                    GameStreamHandler gameStreamHandler = new GameStreamHandler(gameEventHandler.get());
                    JsonStream gameStream = lichessHTTP.stream("https://lichess.org/api/bot/game/stream/" + game, gameStreamHandler);
                    try {
                        gameStream.start(true);
                    } catch (IOException e) {
                        System.out.println("error while opening game stream: " + e);
                    }

                }
        );
        JsonStream eventStream = lichessHTTP.stream("https://lichess.org/api/stream/event", eventStreamHandler);
        eventStream.start(false);

    }

    public static void main(String[] args) throws IOException {

        Set<String> friends = new HashSet<>();
        friends.add("kammaxxer");
        friends.add("blue_kk");

        String authToken = "Bearer BAkKlmZBz3KJKyzI";
        LichessBot lichessBot = new LichessBot(authToken);
        lichessBot.init();

        lichessBot.start(
                gameRequest -> friends.contains(gameRequest.getRequesterId()) && !gameRequest.isRanking(),
                () -> new GameHandler(authToken, "blue_bot_one", new LichessRandomPlayer()));
    }
}
