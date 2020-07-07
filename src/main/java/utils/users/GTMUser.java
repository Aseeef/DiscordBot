package utils.users;

import com.fasterxml.jackson.annotation.*;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.RestAction;
import utils.Data;
import utils.Rank;
import utils.database.DiscordDAO;
import utils.database.sql.BaseDatabase;
import utils.tools.GTools;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

import static utils.tools.GTools.jda;

public class GTMUser {

    /** A maximum of how often should the player's information be updated in minutes */
    private static final int UPDATE_TIME = 30;

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

            // ensure user also exists in database
            try (Connection conn = BaseDatabase.getInstance(BaseDatabase.Database.USERS).getConnection()) {
                if (!DiscordDAO.discordProfileExists(conn, discordId)) {
                    GTMUser.removeGTMUser(discordId);
                    return Optional.empty();
                }
            } catch (SQLException e) {
                GTools.printStackError(e);
            }

            if (gtmUser != null) gtmUser.updateUserDataIfTime();;
            return Optional.ofNullable(gtmUser);
        }
        else return Optional.empty();
    }

    @JsonIgnore
    public static boolean removeGTMUser(long discordId) {
        if (userCache.containsKey(discordId)) {
            userCache.remove(discordId);
            return Data.deleteData(Data.USER, discordId);
        }
        else return false;
    }

    @JsonIgnore
    public static void saveUser (GTMUser gtmUser) {
        Data.storeData(Data.USER, gtmUser, gtmUser.discordId);
    }

    @JsonIgnore
    public void updateUserDataNow() {
        try (Connection conn = BaseDatabase.getInstance(BaseDatabase.Database.USERS).getConnection()) {
            String newUsername = DiscordDAO.getUsername(this.getUuid()).orElse(null);
            Rank newRank = DiscordDAO.getRank(conn, this.getUuid());
            this.setLastUpdated(System.currentTimeMillis());

            if (newRank != null && newRank != this.getRank()) {
                this.setRank(newRank);
                saveUser(this);
            }

            if (newUsername != null && !newUsername.equals(this.username)) {
                this.setUsername(newUsername);
                saveUser(this);
            }

            if (!this.getDiscordMember().getRoles().contains(this.rank.getRole())) {
                if (this.rank.isHigherOrEqualTo(Rank.HELPER) || this.getDiscordMember().isOwner()) {
                    // msg admins TODO
                } else {
                    // set new role on discord
                    this.getDiscordMember().getGuild().addRoleToMember(this.getDiscordId(), rank.getRole()).queue();
                    // remove old role(s)
                    for (Rank r : Rank.values()) {
                        if (r != rank && this.getDiscordMember().getRoles().contains(r.getRole())) {
                            if (rank.isHigherOrEqualTo(Rank.HELPER) || this.getDiscordMember().isOwner()) {
                                // msg admins TODO
                            } else
                                this.getDiscordMember().getGuild().removeRoleFromMember(this.getDiscordId(), r.getRole()).queue();
                        }
                    }
                }
            }

            if (!this.getDiscordMember().getEffectiveName().equals(this.username)) {
                if (!this.rank.isHigherOrEqualTo(Rank.HELPER) && !this.getDiscordMember().isOwner()) {
                    this.getDiscordMember().modifyNickname(username).queue();
                }
            }

            DiscordDAO.updateDiscordTag(conn, this.discordId, this.getDiscordMember().getUser().getAsTag());

        } catch (SQLException e) {
            GTools.printStackError(e);
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
        this.username = username;
    }

    @JsonGetter
    public Rank getRank() {
        return rank;
    }

    @JsonSetter
    public void setRank(Rank rank) {
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

    @JsonIgnore @Deprecated
    /**
     * @deprecated - In some cases, this method may freeze the bot for a few ms while the member is retrieved.
     * This shouldn't cause any issues at our current scale but in the distant future it might!
     */
    public Member getDiscordMember() {
        return jda.getGuilds().get(0).retrieveMemberById(this.discordId).complete();
    }

    @JsonIgnore @Deprecated
    /**
     * @deprecated - In some cases, this method may freeze the bot for a few ms while the user is retrieved.
     * This shouldn't cause any issues at our current scale but in the distant future it might!
     */
    public User getUser() {
        return jda.retrieveUserById(this.discordId).complete();
    }

    @JsonIgnore
    public RestAction<Member> retrieveMember() {
        return jda.getGuilds().get(0).retrieveMemberById(this.discordId);
    }

    @JsonIgnore
    public RestAction<User> retrieveUser() {
        return jda.retrieveUserById(this.discordId);
    }

}
