package utils.database.redis.bitbucket.wrappers.links;

import lombok.Getter;

import javax.annotation.Nullable;

@Getter
public class Links {

    @Nullable private CloneLink[] clone;
    private SelfLink[] self;

}
