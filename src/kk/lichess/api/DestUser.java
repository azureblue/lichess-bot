
package kk.lichess.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DestUser {

    @JsonProperty("id")
    private String id;
    @JsonProperty("name")
    private String name;
    @JsonProperty("title")
    private String title;
    @JsonProperty("rating")
    private int rating;
    @JsonProperty("provisional")
    private boolean provisional;
    @JsonProperty("online")
    private boolean online;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getTitle() {
        return title;
    }

    public int getRating() {
        return rating;
    }

    public boolean isProvisional() {
        return provisional;
    }

    public boolean isOnline() {
        return online;
    }
}
