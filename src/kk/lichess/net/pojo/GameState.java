
package kk.lichess.net.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GameState {

    @JsonProperty("type")
    private String type;
    @JsonProperty("moves")
    private String moves;
    @JsonProperty("wtime")
    private Integer wtime;
    @JsonProperty("btime")
    private Integer btime;
    @JsonProperty("winc")
    private Integer winc;
    @JsonProperty("binc")
    private Integer binc;
    @JsonProperty("bdraw")
    private boolean bdraw;
    @JsonProperty("wdraw")
    private boolean wdraw;


    @JsonProperty("type")
    public String getType() {
        return type;
    }

    @JsonProperty("moves")
    public String getMoves() {
        return moves;
    }

    @JsonProperty("wtime")
    public Integer getWtime() {
        return wtime;
    }

    @JsonProperty("btime")
    public Integer getBtime() {
        return btime;
    }

    @JsonProperty("winc")
    public Integer getWinc() {
        return winc;
    }

    @JsonProperty("binc")
    public Integer getBinc() {
        return binc;
    }

}
