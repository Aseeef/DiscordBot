package Utils.users;

import Utils.Data;
import Utils.Rank;
import Utils.database.DAO;
import com.fasterxml.jackson.annotation.*;
import net.dv8tion.jda.api.entities.Member;

import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

import static Utils.tools.GTools.jda;

public class GTMUser {

    /** A maximum of how often should the player's information be updated in minutes */
    private static final int UPDATE_TIME = 15;

    private UUID uuid;
    private String username;
    private Rank rank;
    private long discordId;
    private long lastUpdated;

    @JsonIgnore
    private static HashMap<Long, GTMUser> userCache = new HashMap<>();

    @JsonCreator
    public GTMUser(@JsonProperty("uuid") UUID uuid, @JsonProperty("username") String username, @JsonProperty("rank") Rank rank, @JsonProperty("discordId") long discordId, @JsonProperty("lastUpdated") long lastUpdated) {
        this.uuid = uuid;
        this.username = username;
        this.rank = rank;
        this.discordId = discordId;
        this.lastUpdated = lastUpdated;

        userCache.put(discordId, this);
    }

    @JsonIgnore
    public GTMUser(UUID uuid, String username, Rank rank, long discordId) {
        this.uuid = uuid;
        this.username = username;
        this.rank = rank;
        this.discordId = discordId;
        this.lastUpdated = System.currentTimeMillis();

        userCache.put(discordId, this);
    }

    @JsonIgnore
    public static Optional<GTMUser> getGTMUser(long discordId) {
        if (userCache.containsKey(discordId)) {
            GTMUser gtmUser = userCache.get(discordId);
            gtmUser.updateUserDataIfTime();
            return Optional.of(userCache.get(discordId));
        }
        else if (Data.exists(Data.USER, discordId)) {
            GTMUser gtmUser = (GTMUser) Data.obtainData(Data.USER, discordId);
            if (gtmUser != null) gtmUser.updateUserDataIfTime();;
            return Optional.ofNullable(gtmUser);
        }
        else return Optional.empty();
    }

    @JsonIgnore
    public static boolean removeGTMUser(long discordId) {
        if (userCache.containsKey(discordId)) {
            userCache.remove(discordId);
            return true;
        }
        else return false;
    }

    @JsonIgnore
    public static void saveUser (GTMUser gtmUser) {
        Data.storeData(Data.USER, gtmUser, gtmUser.discordId);
    }

    @JsonIgnore
    public void updateUserDataNow() {
        String newUsername = DAO.getUsername(this.getUuid()).orElse(null);
        Rank newRank = DAO.getRank(this.getUuid());
        this.setLastUpdated(System.currentTimeMillis());

        if (newRank != null && newRank != this.getRank()) {
            this.setRank(newRank);
            saveUser(this);
        }

        if (newUsername != null && !newUsername.equals(this.username)) {
            this.setUsername(newUsername);
            saveUser(this);
        }
    }

    @JsonIgnore
    public void updateUserDataIfTime() {
        if (this.getLastUpdated() + (UPDATE_TIME * 1000 * 60) < System.currentTimeMillis()) updateUserDataNow();
    }

    @JsonGetter
    public long getDiscordId() {
        return discordId;
    }

    @JsonSetter
    public void setDiscordId(long discordId) {
        this.discordId = discordId;
    }

    @JsonGetter
    public UUID getUuid() {
        return uuid;
    }

    @JsonSetter
    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    @JsonGetter
    public String getUsername() {
        return username;
    }

    @JsonSetter
    public void setUsername(String username) {
        if (!rank.isHigherOrEqualTo(Rank.HELPER)) {
            // update nick on discord
            this.getDiscordMember().modifyNickname(username).queue();
        }
        this.username = username;
    }

    @JsonGetter
    public Rank getRank() {
        return rank;
    }

    @JsonSetter
    public void setRank(Rank rank) {
        if (!rank.isHigherOrEqualTo(Rank.HELPER)) {
            // set new role on discord
            this.getDiscordMember().getGuild().addRoleToMember(this.getDiscordId(), rank.getRole()).queue();
            // remove old role(s)
            for (Rank r : Rank.values()) {
                if (r != rank)
                    this.getDiscordMember().getGuild().removeRoleFromMember(this.getDiscordId(), r.getRole()).queue();
            }
        }

        this.rank = rank;
    }

    @JsonGetter
    public long getLastUpdated() {
        return lastUpdated;
    }

    @JsonSetter
    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    @JsonIgnore
    public Member getDiscordMember() {
        return jda.getGuilds().get(0).getMemberById(this.getDiscordId());
    }

}
