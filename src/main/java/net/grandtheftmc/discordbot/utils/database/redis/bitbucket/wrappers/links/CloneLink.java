package net.grandtheftmc.discordbot.utils.database.redis.bitbucket.wrappers.links;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class CloneLink {

    private String href;
    @JsonProperty("name") private String protocol;

}
