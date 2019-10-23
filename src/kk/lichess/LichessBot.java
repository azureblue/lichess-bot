package kk.lichess;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kk.lichess.api.Challenge;
import kk.lichess.api.Side;
import kk.lichess.bots.LichessRandomPlayer;
import kk.lichess.net.JsonStream;
import kk.lichess.net.LichessHTTP;
import kk.lichess.net.LichessStreamGroup;
import org.junit.jupiter.api.parallel.Resources;
import spark.Spark;
import spark.utils.SparkUtils;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class LichessBot {
    private final LichessHTTP lichessHTTP;
    private final LichessStreamGroup lichessGames = new LichessStreamGroup();
    private final LichessStreamGroup lichessEvents = new LichessStreamGroup();

    public LichessBot(String authToken) {
        lichessHTTP = new LichessHTTP(authToken);
    }

    public void init() {
        try {
            JsonNode user = lichessHTTP.get("/api/account").toJson();
            if (user.has("error")) {
                throw new IllegalStateException("error: " + user.get("error"));
            }

        } catch (JsonProcessingException e) {
            throw new IllegalStateException("error: " + e);
        }

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

                    if (challengeAcceptResult.contains("error")) {
                        System.out.println("lichess error: error while answering challenge: " + challengeAcceptResult);
                    }
                },

                game -> {
                    GameStreamHandler gameStreamHandler = new GameStreamHandler(gameEventHandler.get());
                    JsonStream gameStream = lichessHTTP.stream("https://lichess.org/api/bot/game/stream/" + game, gameStreamHandler);
                    try {
                        gameStream.start(true, lichessGames);
                    } catch (IOException e) {
                        System.out.println("error while opening game stream: " + e);
                    }

                }
        );
        JsonStream eventStream = lichessHTTP.stream("https://lichess.org/api/stream/event", eventStreamHandler);
        eventStream.start(true, lichessEvents);

        Spark.port(4567);

        Spark.get("lichess-bot/pid", (req, res) -> "" + ProcessHandle.current().pid());
        Spark.get("lichess-bot/games", (req, res) -> lichessGames.size());
        Spark.get("lichess-bot/stop/", (req, res) -> {
            lichessEvents.stopAll();
            lichessGames.stopAll();
            return "\"ok\"";
        });

        Spark.get("lichess-bot/kill", (req, res) -> {
            System.exit(0);
            return "";
        });

    }

    public static void main(String[] args) throws IOException {
        JsonNode config = new ObjectMapper().readTree(LichessBot.class.getResourceAsStream("/lichess-bot.json"));
        String authToken = config.get("authToken").asText();
        Set<String> friends = new HashSet<>();
        config.get("friends").elements().forEachRemaining(node -> friends.add(node.asText()));

        System.out.println(friends);

        LichessBot lichessBot = new LichessBot(authToken);
        lichessBot.init();

        lichessBot.start(
                gameRequest -> friends.contains(gameRequest.getRequesterId()) && !gameRequest.isRanking(),
                () -> new GameHandler(authToken, "blue_bot_one", new LichessRandomPlayer()));

    }
}
