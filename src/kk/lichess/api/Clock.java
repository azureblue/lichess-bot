
package kk.lichess.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "initial",
    "increment"
})
public class Clock {

    @JsonProperty("initial")
    private Integer initial;
    @JsonProperty("increment")
    private Integer increment;

    @JsonProperty("initial")
    public Integer getInitial() {
        return initial;
    }

    @JsonProperty("initial")
    public void setInitial(Integer initial) {
        this.initial = initial;
    }

    @JsonProperty("increment")
    public Integer getIncrement() {
        return increment;
    }

    @JsonProperty("increment")
    public void setIncrement(Integer increment) {
        this.increment = increment;
    }

}
