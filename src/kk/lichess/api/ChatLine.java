
package kk.lichess.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)

public class ChatLine {

    @JsonProperty("type")
    private String type;
    @JsonProperty("username")
    private String username;
    @JsonProperty("text")
    private String text;
    @JsonProperty("room")
    private String room;

    @JsonProperty("type")
    public String getType() {
        return type;
    }

    @JsonProperty("username")
    public String getUsername() {
        return username;
    }

    @JsonProperty("text")
    public String getText() {
        return text;
    }

    @JsonProperty("room")
    public String getRoom() {
        return room;
    }


}
