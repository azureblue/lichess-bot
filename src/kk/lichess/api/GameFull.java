
package kk.lichess.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class GameFull {

    @JsonProperty("type")
    private String type;
    @JsonProperty("id")
    private String id;
    @JsonProperty("rated")
    private Boolean rated;
    @JsonProperty("variant")
    private Variant variant;
    @JsonProperty("clock")
    private Clock clock;
    @JsonProperty("speed")
    private String speed;
    @JsonProperty("perf")
    private Perf perf;
    @JsonProperty("createdAt")
    private long createdAt;
    @JsonProperty("white")
    private Player white;
    @JsonProperty("black")
    private Player black;
    @JsonProperty("initialFen")
    private String initialFen;
    @JsonProperty("state")
    private GameState gameState;


    @JsonProperty("type")
    public String getType() {
        return type;
    }

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @JsonProperty("rated")
    public Boolean getRated() {
        return rated;
    }

    @JsonProperty("variant")
    public Variant getVariant() {
        return variant;
    }

    @JsonProperty("clock")
    public Clock getClock() {
        return clock;
    }

    @JsonProperty("speed")
    public String getSpeed() {
        return speed;
    }

    @JsonProperty("perf")
    public Perf getPerf() {
        return perf;
    }

    @JsonProperty("createdAt")
    public long getCreatedAt() {
        return createdAt;
    }

    @JsonProperty("white")
    public Player getWhite() {
        return white;
    }

    @JsonProperty("black")
    public Player getBlack() {
        return black;
    }

    @JsonProperty("initialFen")
    public String getInitialFen() {
        return initialFen;
    }

    @JsonProperty("gameState")
    public GameState getGameState() {
        return gameState;
    }

}
