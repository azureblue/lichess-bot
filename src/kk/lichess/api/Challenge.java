
package kk.lichess.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Challenge {

    @JsonProperty("id")
    private String id;
    @JsonProperty("status")
    private String status;
    @JsonProperty("challenger")
    private Challenger challenger;
    @JsonProperty("destUser")
    private DestUser destUser;
    @JsonProperty("variant")
    private Variant variant;
    @JsonProperty("rated")
    private Boolean rated;
    @JsonProperty("speed")
    private String speed;
    @JsonProperty("timeControl")
    private TimeControl timeControl;
    @JsonProperty("color")
    private String color;
    @JsonProperty("perf")
    private Perf perf;


    public String getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }

    public Challenger getChallenger() {
        return challenger;
    }

    public DestUser getDestUser() {
        return destUser;
    }

    public Variant getVariant() {
        return variant;
    }

    public boolean isRated() {
        return rated;
    }

    public String getSpeed() {
        return speed;
    }

    public TimeControl getTimeControl() {
        return timeControl;
    }

    public String getColor() {
        return color;
    }

    public Perf getPerf() {
        return perf;
    }
}
