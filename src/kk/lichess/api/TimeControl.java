
package kk.lichess.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TimeControl {

    @JsonProperty("type")
    private String type;
    @JsonProperty("limit")
    private Integer limit;
    @JsonProperty("increment")
    private Integer increment;
    @JsonProperty("show")
    private String show;


    public String getType() {
        return type;
    }

    public Integer getLimit() {
        return limit;
    }

    public Integer getIncrement() {
        return increment;
    }

    public String getShow() {
        return show;
    }
}
