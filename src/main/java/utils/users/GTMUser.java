package utils.users;

import com.fasterxml.jackson.annotation.*;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import utils.Data;
import utils.MembersCache;
import utils.console.Logs;
import utils.database.DiscordDAO;
import utils.database.sql.BaseDatabase;
import utils.tools.GTools;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

public class GTMUser {

    /** A maximum of how often should the player's information be updated in minutes */
    private static final int UPDATE_TIME = 30;

    private UUID uuid;
    private String username;
    private Rank rank;
    private long discordId;
    private long lastUpdated;

    @JsonIgnore
    private Optional<Member> optionalMember;

    @JsonIgnore
    private static HashMap<Long, GTMUser> userCache = new HashMap<>();

    @JsonCreator
    public GTMUser(@JsonProperty("uuid") UUID uuid, @JsonProperty("username") String username, @JsonProperty("rank") Rank rank, @JsonProperty("discordId") long discordId, @JsonProperty("lastUpdated") long lastUpdated) {
        this.uuid = uuid;
        this.username = username;
        this.rank = rank;
        this.discordId = discordId;
        this.lastUpdated = lastUpdated;
        this.optionalMember = MembersCache.getMember(this.discordId);

        userCache.put(discordId, this);
        saveUser(this);
    }

    @JsonIgnore
    public GTMUser(UUID uuid, String username, Rank rank, long discordId) {
        this.uuid = uuid;
        this.username = username;
        this.rank = rank;
        this.discordId = discordId;
        this.lastUpdated = System.currentTimeMillis();
        this.optionalMember = MembersCache.getMember(this.discordId);

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
    public static List<GTMUser> loadAndGetAllUsers() {
        List<GTMUser> users = new ArrayList<>(userCache.values());
        for (long dataId : Data.getDataList(Data.USER)) {
            boolean alreadyLoaded = users.stream().anyMatch( (loadedUser) -> dataId == loadedUser.getDiscordId());
            if (!alreadyLoaded)
                users.add((GTMUser) Data.obtainData(Data.USER, dataId));
        }
        return users;
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
        if (!this.optionalMember.isPresent()) {
            Logs.log("Skipping data update for GTM Player " + this.getUsername() + " because they have left this discord!");
            return;
        }

        Logs.log("Attempting to update user data for GTM Player " + this.getUsername() + "!");
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

            Member discordMember = this.optionalMember.get();

            if (!discordMember.getRoles().contains(this.rank.getRole())) {
                if (this.rank.isHigherOrEqualTo(Rank.HELPER) || discordMember.isOwner()) {
                    // msg admins TODO
                } else {
                    // set new role on discord
                    discordMember.getGuild().addRoleToMember(this.getDiscordId(), rank.getRole()).queue();
                    // remove old role(s)
                    for (Rank r : Rank.values()) {
                        if (r != rank && discordMember.getRoles().contains(r.getRole())) {
                            if (rank.isHigherOrEqualTo(Rank.HELPER) || discordMember.isOwner()) {
                                // msg admins TODO
                            } else
                                discordMember.getGuild().removeRoleFromMember(this.getDiscordId(), r.getRole()).queue();
                        }
                    }
                }
            }

            if (!discordMember.getEffectiveName().equals(this.username)) {
                if (!this.rank.isHigherOrEqualTo(Rank.HELPER) && !discordMember.isOwner()) {
                    discordMember.modifyNickname(username).queue();
                }
            }

            DiscordDAO.updateDiscordTag(conn, this.discordId, discordMember.getUser().getAsTag());

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

    @JsonIgnore
    public boolean isMember() {
        return this.optionalMember.isPresent();
    }

    @JsonIgnore
    public Optional<Member> getDiscordMember() {
        return this.optionalMember;
    }

    @JsonIgnore
    public Optional<User> getUser() {
        return this.optionalMember.map(Member::getUser);
    }

}
