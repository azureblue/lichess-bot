package kk.lichess.net.service;

import kk.lichess.Log;
import kk.lichess.net.LichessHTTP;

import java.util.Optional;

public class CatFact {

    public Optional<String> catFact() {
        try {
            String catFact = LichessHTTP
                    .get("https://catfact.ninja/fact?max_length=100")
                    .toJson()
                    .get("fact")
                    .asText();

            return Optional.ofNullable(catFact);
        } catch (Exception e) {
            Log.e("no cat fact :(", e);
            return Optional.empty();
        }
    }
}
