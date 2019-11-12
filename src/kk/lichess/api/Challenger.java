
package kk.lichess.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

public class Challenger {

    @JsonProperty("id")
    private String id;
    @JsonProperty("name")
    private String name;
    @JsonProperty("title")
    private Object title;
    @JsonProperty("rating")
    private Integer rating;
    @JsonProperty("online")
    private boolean online;
    @JsonProperty("provisional")
    private boolean provisional;
    @JsonProperty("lag")
    private Integer lag;


    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Object getTitle() {
        return title;
    }

    public Integer getRating() {
        return rating;
    }

    public Boolean getOnline() {
        return online;
    }

    public Integer getLag() {
        return lag;
    }

    public boolean isOnline() {
        return online;
    }

    public boolean isProvisional() {
        return provisional;
    }
}
