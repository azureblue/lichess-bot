package kk.lichess.net.service;

import kk.lichess.Log;
import kk.lichess.net.LichessHTTP;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class LichessGameStatus {

    public Optional<Set<String>> gamesInProgress() {
        HashSet<String> res = new HashSet<>();
        try {
            LichessHTTP
                    .get("https://lichess.org/api/account/playing")
                    .toJson()
                    .get("nowPlaying")
                    .elements()
                    .forEachRemaining(gameNode -> {
                        res.add(gameNode.get("fullId").asText());
                        res.add(gameNode.get("gameId").asText());
                    });
            Log.v("ongoing games: " + res);
            return Optional.of(res);
        } catch (Exception e) {
            Log.e("invalid games status", e);
            return Optional.empty();
        }
    }

}
