package kk.lichess;

import kk.lichess.api.Challenge;
import kk.lichess.net.InputStreamSupplier;
import kk.lichess.net.LichessStream;

import java.util.function.Consumer;

public class EventStream extends LichessStream {

    public EventStream(InputStreamSupplier stream, Consumer<Challenge> challengeHandler, Consumer<String> gameStartHandler) {
        super(stream, (stre, result) -> {
            Log.i("event stream ended: " + result.getResultStatus());
        }, new EventHandler(challengeHandler, gameStartHandler));
    }
}
