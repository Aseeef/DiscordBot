package Utils.users;

import Utils.Data;
import Utils.Rank;

import java.util.HashMap;
import java.util.UUID;

public class GTMUser {

    private UUID uuid;
    private String username;
    private Rank rank;
    private long discordId;

    private static HashMap<Long, GTMUser> userCache = new HashMap<>();

    public GTMUser(UUID uuid, String username, Rank rank, long discordId) {
        this.uuid = uuid;
        this.username = username;
        this.rank = rank;
        this.discordId = discordId;

        userCache.put(discordId, this);
    }

    public static GTMUser getGTMUser(long discordId) {
        if (userCache.containsKey(discordId))
            return userCache.get(discordId);
        else return (GTMUser) Data.obtainData(Data.USER, discordId);
    }

    public static boolean removeGTMUser(long discordId) {
        if (userCache.containsKey(discordId)) {
            userCache.remove(discordId);
            return true;
        }
        else return false;
    }

    public long getDiscordId() {
        return discordId;
    }

    public void setDiscord(long discordId) {
        this.discordId = discordId;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
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

}
