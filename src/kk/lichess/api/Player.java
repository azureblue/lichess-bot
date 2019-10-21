
package kk.lichess.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "name",
    "provisional",
    "rating",
    "title"
})
public class Player {

    @JsonProperty("id")
    private String id;
    @JsonProperty("name")
    private String name;
    @JsonProperty("provisional")
    private Boolean provisional;
    @JsonProperty("rating")
    private Integer rating;
    @JsonProperty("title")
    private String title;

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("provisional")
    public Boolean getProvisional() {
        return provisional;
    }

    @JsonProperty("rating")
    public Integer getRating() {
        return rating;
    }

    @JsonProperty("title")
    public String getTitle() {
        return title;
    }

}
