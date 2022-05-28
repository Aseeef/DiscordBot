package net.grandtheftmc.discordbot.utils.database.redis.bitbucket.wrappers;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import net.grandtheftmc.discordbot.utils.database.redis.bitbucket.wrappers.links.Links;

@Getter
public class Repository {

    private String slug;
    private int id;
    private String hierarchyId;
    private String name;
    private String scmId;
    private String state;
    private String statusMessage;
    private boolean forkable;
    private Project project;
    @JsonProperty("public") private boolean isPublic;
    private Links links;

}
