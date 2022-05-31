package net.grandtheftmc.discordbot.xenforo.objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

@Deprecated
@JsonRootName("user") @JsonIgnoreProperties ({"gravatar"})
public class XenforoUserJson {

    private final int userId;
    private final String username;
    private final String gender;
    private final long avatarDate;

    @JsonCreator
    public XenforoUserJson(@JsonProperty ("user_id") int userId,
                           @JsonProperty ("username") String username,
                           @JsonProperty ("gender") String gender,
                           @JsonProperty ("avatar_date") long avatarDate) {
        this.userId = userId;
        this.username = username;
        this.gender = gender;
        this.avatarDate = avatarDate;
    }

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getGender() {
        return gender;
    }

    public long getAvatarDate() {
        return avatarDate;
    }
}
