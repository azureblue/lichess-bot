package kk.lichess;

import kk.lichess.bots.ChessBotVerbosePlayer;
import kk.lichess.bots.api.ChessPlayer;
import kk.lichess.game.GameChatInterface;
import kk.lichess.game.GameHandler;
import kk.lichess.game.GameMoveInterface;
import kk.lichess.game.Player;
import kk.lichess.net.LichessHTTP;
import kk.lichess.net.LichessHTTPException;
import kk.lichess.net.LichessStream;
import kk.lichess.net.pojo.ChatLine;
import kk.lichess.net.pojo.GameFull;
import kk.lichess.net.pojo.GameState;
import kk.lichess.net.service.CatFact;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import static java.util.Optional.ofNullable;
import static java.util.concurrent.CompletableFuture.runAsync;
import static java.util.concurrent.CompletableFuture.supplyAsync;

public class LichessGames {
    private final String playerId;
    private final LichessHTTP lichessHTTP;

    private final Supplier<ChessPlayer> chessPlayerSupplier;
    private final Map<String, StreamWrapper> streams = new HashMap<>();
    private volatile boolean restartOnError = true;

    public LichessGames(LichessHTTP lichessHTTP, String playerId, Supplier<ChessPlayer> chessPlayerSupplier) {
        this.lichessHTTP = lichessHTTP;
        this.playerId = playerId;
        this.chessPlayerSupplier = chessPlayerSupplier;
    }

    public synchronized int size() {
        return streams.size();
    }

    public synchronized void stop(String gameId) {
        ofNullable(streams.get(gameId)).ifPresent(sw -> sw.stream.stop());
    }

    public synchronized void stopAll() {
        restartOnError = false;
        streams.forEach((s, sw) -> sw.stream.stop());
    }

    public synchronized void startAll() {

    }

    public synchronized void abortGame(String gameId) {
        Log.v("aborting game " + gameId);
        StreamWrapper stream = streams.get(gameId);
        if (stream == null) {
            Log.e("stream not present " + gameId);
            throw new IllegalArgumentException("no such game " + gameId);
        }
        stream.stream.stop();
    }

    private void handleGameError(String gameId) {
        tryRestartGameStream(gameId);
    }

    public void startGame(String gameId) {
        runAsync(() -> {
            try {
                Supplier<GameHandler> gameHandlerSupplier =
                        () -> new Player(new ChessBotVerbosePlayer(chessPlayerSupplier.get(), gameId), gameId);
                start(gameId, gameHandlerSupplier);
            } catch (IOException e) {
                Log.e("unable to start game: ", e);
            }
        });
    }

    private void handleStreamFinish(String gameId, LichessStream stream, LichessStream.StreamResult result) {
        StreamWrapper streamWrapper = removeStream(gameId);
        runAsync(() -> {
            if (result.getResultStatus() == LichessStream.StreamResultStatus.Error) {
                Throwable throwable = result.getThrowable();
                if (throwable == null)
                    Log.e("stream ended with error");
                else
                    Log.e("stream ended with error", throwable);

                Log.d("trying to restart game " + gameId);
                startIfStillInProgress(gameId);
            } else if (streamWrapper.restart) {
                startIfStillInProgress(gameId);
            }
        });
    }

    private synchronized void start(String gameId, Supplier<GameHandler> gameHandler) throws IOException {
        Log.d(this.getClass().getSimpleName(), "start()");
        if (streams.containsKey(gameId)) {
            Log.e("stream " + gameId + "already exists!");
            throw new IllegalStateException("stream " + gameId + "already exists!");
        }
        LichessStream gameStream = lichessHTTP.gameStream(
                (stream, result) -> handleStreamFinish(gameId, stream, result),
                gameId, createHandler(gameId, gameHandler.get()));
        streams.put(gameId, new StreamWrapper(gameStream));
        gameStream.start();
    }

    private GameEventHandler createHandler(String gameId, GameHandler gameHandler) {
        return new GameEventHandler() {
            CatFact catFact = new CatFact();
            Side mySide = null;
            GameMoveInterface moveInterface = createMoveInterface(gameId);

            @Override
            public void handleGameFull(GameFull gameFull) {
                gameHandler.handleGameStart(
                        gameFull.getInitialFen(),
                        mySide = getMySide(gameFull),
                        gameFull.getClock().getInitial(),
                        gameFull.getClock().getIncrement());

                Log.i("game " + gameId + "started | opponent: " + opponentId(gameFull));

                catFact.catFact().ifPresent(fact -> createGameChatInterface(gameId).sendChat("Cat fact: " + fact));

                this.handleGameState(gameFull.getGameState());
            }

            @Override
            public void handleGameState(GameState gameState) {
                gameHandler.handleGameState(movesFromGameState(gameState), moveInterface);
            }

            @Override
            public void handleChatLine(ChatLine chatLine) {
                if (chatLine.getUsername().equals("lichess")
                        && chatLine.getText().toLowerCase().equals("" + (mySide.isWhite() ? "black" : "white") + " offers draw")) {
                    gameHandler.handleDrawOffer();
                }
            }

            private ArrayList<String> movesFromGameState(GameState gameState) {
                ArrayList<String> moves = new ArrayList<>();
                String movesString = gameState.getMoves();
                if (movesString.trim().equals(""))
                    return moves;

                for (String move : movesString.split(" ")) {
                    if (move.trim().equals(""))
                        continue;
                    moves.add(move);
                }
                return moves;
            }

            private String opponentId(GameFull gameFull) {
                if (gameFull.getWhite().getId().equals(playerId))
                    return gameFull.getBlack().getId();
                else if (gameFull.getBlack().getId().equals(playerId))
                    return gameFull.getWhite().getId();

                Log.e("bot player id doesn't match");
                throw new IllegalStateException("bot player id doesn't match");
            }

            private Side getMySide(GameFull gameFull) {
                if (gameFull.getWhite().getId().equals(playerId))
                    return Side.White;
                else if (gameFull.getBlack().getId().equals(playerId))
                    return Side.Black;

                Log.e("bot player id doesn't match");
                throw new IllegalStateException("bot player id doesn't match");
            }
        };
    }

    private synchronized StreamWrapper removeStream(String gameId) {
        return streams.remove(gameId);
    }

    private synchronized void tryRestartGameStream(String gameId) {
        if (!restartOnError) {
            Log.v("not restarting");
            return;
        }
        Log.d(this.getClass().getName(), "tryRestartGameStream(" + gameId + ")");
        StreamWrapper wrapper = streams.get(gameId);
        if (wrapper == null) {
            Log.d("game wrapper not present | restarting if needed");
            startIfStillInProgress(gameId);
            return;
        }

        wrapper.scheduleForRestart();
        wrapper.stream.stop();
    }

    private void startIfStillInProgress(String gameId) {
        runAsync(() -> {
            Log.d(this.getClass().getName(), "startIfStillInProgress(" + gameId + ")");
            try {
                Set<String> gamesInProgress = lichessHTTP.gamesInProgress();

                if (gamesInProgress.contains(gameId)) {
                    synchronized (LichessGames.this) {
                        if (!streams.containsKey(gameId))
                            startGame(gameId);
                    }
                } else
                    Log.i("game " + gameId + " is not in progress");
            } catch (LichessHTTPException e) {
                Log.e("error getting game in progress", e);
                throw new IllegalStateException("error getting game in progress");
            }
        });
    }

    private void startAllGamesInProgress() {
        runAsync(() -> {
            Log.d(this.getClass().getName(), "startAllGamesInProgress()");
            try {
                Set<String> gamesInProgress = lichessHTTP.gamesInProgress();
                synchronized (LichessGames.this) {
                    gamesInProgress.forEach(gameId -> {
                        if (!streams.containsKey(gameId)) {
                            startGame(gameId);
                        }
                    });
                }
            } catch (LichessHTTPException e) {
                Log.e("error getting game in progress", e);
                throw new IllegalStateException("error getting game in progress");
            }
        });
    }

    private GameChatInterface createGameChatInterface(String gameId) {
        return message -> supplyAsync(
                () -> lichessHTTP.postChatMessage(gameId, LichessHTTP.Room.Player, message)).whenComplete(
                (lichessResponse, throwable) -> {
                    if (throwable != null) {
                        Log.e("post chat: ", throwable);
                    } else if (lichessResponse.getStatusCode() != 200) {
                        Log.e("message error: " + lichessResponse.getContent());
                    }
                }
        );
    }

    private GameMoveInterface createMoveInterface(String gameId) {
        return (move, acceptDraw) -> supplyAsync(() -> lichessHTTP.postMove(gameId, move, acceptDraw)).whenComplete(
                (response, ex) -> {
                    if (ex != null) {
                        Log.e("game " + gameId + ": " + "post move error: ", ex);
                        handleGameError(gameId);
                    } else if (response.getStatusCode() != 200) {
                        Log.e("game " + gameId + ": " + "post move error : " + response.getContent());
                        handleGameError(gameId);
                    } else
                        Log.v("game " + gameId + ": " + "move " + move + " sent");
                }
        );
    }

    private static class StreamWrapper {
        private final LichessStream stream;
        private boolean restart = false;

        StreamWrapper(LichessStream stream) {
            this.stream = stream;
        }

        void scheduleForRestart() {
            restart = true;
        }
    }
}
