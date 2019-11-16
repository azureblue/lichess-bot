package kk.lichess.net.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Clock {

    @JsonProperty("initial")
    private Integer initial;
    @JsonProperty("increment")
    private Integer increment;

    @JsonProperty("initial")
    public Integer getInitial() {
        return initial;
    }

    @JsonProperty("increment")
    public Integer getIncrement() {
        return increment;
    }

}
