package Utils;

public class Users {

    private String uuid;
    private String username;
    private Rank rank;
    // discord[0] is id and discord[1] is tag
    private String[] discord;

    public Users(String uuid, String username, Rank rank, String[] discord) {
        this.uuid = uuid;
        this.username = username;
        this.rank = rank;
        this.discord = discord;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Rank getRank() {
        return rank;
    }

    public void setRank(Rank rank) {
        this.rank = rank;
    }

    public String[] getDiscord() {
        return discord;
    }

    public void setDiscord(String[] discord) {
        this.discord = discord;
    }

}
