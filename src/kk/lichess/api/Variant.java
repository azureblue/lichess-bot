
package kk.lichess.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Variant {

    @JsonProperty("key")
    private String key;
    @JsonProperty("name")
    private String name;
    @JsonProperty("short")
    private String shortName;


    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public String getShortName() {
        return shortName;
    }
}
