package kk.lichess.net.service;

import kk.lichess.Log;
import kong.unirest.Unirest;

import java.util.Optional;

public class CatFact {

    public Optional<String> catFact() {
        try {
            String catFact = null;

            catFact = getFact();
            if (catFact.contains("\\\\"))
                catFact = getFact();
            if (catFact.contains("\\\\"))
                catFact = getFact();

            return Optional.ofNullable(catFact);
        } catch (Exception e) {
            Log.e("no cat fact :(", e);
            return Optional.empty();
        }
    }

    private String getFact() {
        return Unirest
                .get("https://catfact.ninja/fact?max_length=100")
                .asJson().getBody().getObject().getString("fact");
    }
}
