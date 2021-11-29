package xenforo.objects;

import lombok.Getter;

@Getter
public class XenforoUser {

    private int userId;
    private String username;
    private String customTitle;
    private String email;
    private String timezone;
    private String gender;
    private String userState;
    private int registerDate;

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
