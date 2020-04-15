package Utils;

public class Users {

    private String uuid;
    private String username;
    private Rank rank;
    private long discordId;

    public Users(String uuid, String username, Rank rank, long discordId) {
        this.uuid = uuid;
        this.username = username;
        this.rank = rank;
        this.discordId = discordId;
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

    public long getDiscordId() {
        return discordId;
    }

    public void setDiscord(long discordId) {
        this.discordId = discordId;
    }

}
