package utils.database.redis.bitbucket.wrappers;

import lombok.Getter;
import utils.database.redis.bitbucket.wrappers.links.Links;

@Getter
public class GitUser {

    private String name;
    private String emailAddress;
    private int id;
    private String displayName;
    private boolean active;
    private String slug;
    private String type;
    private Links links;

}
