package kk.lichess;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kk.lichess.api.Challenge;
import kk.lichess.api.Side;
import kk.lichess.bots.api.ChessPlayer;
import kk.lichess.net.LichessGames;
import kk.lichess.net.LichessHTTP;
import kk.lichess.net.LichessHTTPException;
import spark.Spark;

import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static java.util.Arrays.asList;

public class LichessBot {


    private static final String PLAYER_ID = "blue_bot_one";
    private LichessGames lichessGames;

    public static void main(String[] args) throws Exception {

        String chessPlayerClassName = args[0];

        if (chessPlayerClassName == null)
            throw new IllegalArgumentException("missing player class name");

        Class<?> playerClass = ClassLoader.getSystemClassLoader().loadClass(chessPlayerClassName);
        if (!new HashSet<>(asList(playerClass.getInterfaces())).contains((ChessPlayer.class)))
            throw new IllegalArgumentException("class " + chessPlayerClassName + " is not an instance of ChessPlayer");

        Constructor<?> constructor = playerClass.getDeclaredConstructor();

        constructor.newInstance();

        Supplier<ChessPlayer> chessPlayerSupplier = () -> {
            try {
                return (ChessPlayer) constructor.newInstance();
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        };

        System.setOut(new PrintStream(System.out, true, "UTF-8"));

        JsonNode config = new ObjectMapper().readTree(LichessBot.class.getResourceAsStream("/lichess-bot.json"));
        Set<String> friends = new HashSet<>();
        config.get("friends").elements().forEachRemaining(node -> friends.add(node.asText()));

        LichessBot lichessBot = new LichessBot();
        lichessBot.init();


        Predicate<GameRequest> gameRequestAccept = gameRequest
                -> friends.contains(gameRequest.getRequesterId())
                || gameRequest.getTime() <= 10 * 60;

        lichessBot.start(gameRequestAccept, chessPlayerSupplier);
    }

    private void init() {
        try {
            JsonNode user = LichessHTTP.get("https://lichess.org/api/account").toJson();
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

    public void start(Predicate<GameRequest> acceptGamePredicate, Supplier<ChessPlayer> chessPlayerSupplier) throws InterruptedException {
        lichessGames = new LichessGames(PLAYER_ID, chessPlayerSupplier);

        Consumer<Challenge> challengeHandler = challenge -> {
            GameRequest gameRequest = gameRequestFromChallenge(challenge);

            boolean challengeAccepted = acceptGamePredicate.and(ignore -> lichessGames.size() < 4).test(gameRequest)
                    && (challenge.getVariant().getKey().equals("standard") || challenge.getVariant().getShortName().equals("FEN"));

            String challengeAcceptString = challengeAccepted ? "accept" : "decline";
            String challengeAcceptResult = LichessHTTP.post("https://lichess.org/api/challenge/"
                    + challenge.getId() + "/"
                    + challengeAcceptString).getContent();

            Log.d("challenge from " + challenge.getChallenger().getId() + ": " + challengeAcceptString);

            if (challengeAcceptResult.contains("error")) {
                Log.e("lichess error: error while answering challenge: " + challengeAcceptResult);
            }
        };

        Consumer<String> gameStartHandler = game -> {
            Log.i("starting game: " + game);
            lichessGames.startGame(game);

        };

        Spark.port(4567);

        Spark.get("lichess-bot/pid", (req, res) -> "" + ProcessHandle.current().pid());
        Spark.get("lichess-bot/games", (req, res) -> lichessGames.size());
        Spark.get("lichess-bot/stop/", (req, res) -> {
            lichessGames.stopAll();
            return "\"ok\"";
        });

        Spark.get("lichess-bot/kill", (req, res) -> {
            System.exit(0);
            return "";
        });

        while (true) {
            try {
                Log.i("opening event stream");
                EventStream eventStream = new EventStream(LichessHTTP.stream(
                        "https://lichess.org/api/stream/event"), challengeHandler, gameStartHandler);

                eventStream.start();
                eventStream.sync();
            } catch (IOException | LichessHTTPException e) {
                Log.e("event stream error", e);
            }
            Thread.sleep(2000);
        }
    }
}
