package utils.database.redis.bitbucket.wrappers;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Getter;
import utils.database.redis.bitbucket.wrappers.links.Links;

@Getter
@JsonRootName("project")
public class Project {

    private String key;
    private int id;
    private String name;
    private String description;
    @JsonProperty("public") private boolean isPublic;
    private String type;
    private Links links;

}
