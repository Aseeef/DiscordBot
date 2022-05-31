package net.grandtheftmc.discordbot.utils.database.redis.bitbucket.wrappers;

import lombok.Getter;

@Getter
public class Commit {

    private String id;
    private String displayId;
    private GitUser author;
    private long authorTimestamp;
    private GitUser committer;
    private long committerTimestamp;
    private String message;
    private Parent[] parents;

}
