
package kk.lichess.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Perf {

    @JsonProperty("icon")
    private String icon;
    @JsonProperty("name")
    private String name;


    public String getIcon() {
        return icon;
    }

    public String getName() {
        return name;
    }

}
