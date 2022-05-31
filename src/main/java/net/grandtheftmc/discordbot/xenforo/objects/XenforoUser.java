package net.grandtheftmc.discordbot.xenforo.objects;

import lombok.Getter;

@Getter
public class XenforoUser {

    private final int userId;
    private final String username;
    private final String customTitle;
    private final String email;
    private final String timezone;
    private final String gender;
    private final String userState;
    private final int registerDate;

    public XenforoUser(int userId, String username, String customTitle, String email, String timezone, String gender, String userState, int registerDate) {
        this.userId = userId;
        this.username = username;
        this.customTitle = customTitle;
        this.email = email;
        this.timezone = timezone;
        this.gender = gender;
        this.userState = userState;
        this.registerDate = registerDate;
    }

    public String getProfileLink() {
        return "https://grandtheftmc.net/members/" + this.username.toLowerCase() + "." + userId + "/";
    }
}
