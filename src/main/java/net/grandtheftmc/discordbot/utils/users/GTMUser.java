package net.grandtheftmc.discordbot.utils.users;

import com.fasterxml.jackson.annotation.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import net.grandtheftmc.discordbot.GTMBot;
import net.grandtheftmc.discordbot.utils.Utils;
import net.grandtheftmc.discordbot.utils.database.DiscordDAO;
import net.grandtheftmc.discordbot.utils.database.sql.BaseDatabase;
import net.grandtheftmc.discordbot.utils.threads.ThreadUtil;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.HierarchyException;
import net.grandtheftmc.discordbot.utils.Data;
import net.grandtheftmc.discordbot.utils.MembersCache;
import net.grandtheftmc.discordbot.utils.console.Logs;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

@EqualsAndHashCode
public class GTMUser {

    /** A maximum of how often should the player's information be updated in minutes */
    private static final int UPDATE_TIME = 60;
    @Setter @Getter
    private static boolean disableDataUpdates = false;

    private UUID uuid;
    private String username;
    private Rank rank;
    private long discordId;
    //due to async updates, this multithreading keyword 'volatile' is needed to update visibility
    private volatile long lastUpdated;

    @JsonIgnore
    private final Optional<Member> optionalMember;

    @JsonIgnore
    private static final HashMap<Long, GTMUser> userCache = new HashMap<>();

    @JsonCreator
    public GTMUser(@JsonProperty("uuid") String uuid, @JsonProperty("username") String username, @JsonProperty("rank") Rank rank, @JsonProperty("discordId") long discordId, @JsonProperty("lastUpdated") long lastUpdated) {
        this.uuid = UUID.fromString(uuid);
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
            return Optional.of(gtmUser);
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
                Utils.printStackError(e);
            }

            if (gtmUser != null) gtmUser.updateUserDataIfTime();
            return Optional.ofNullable(gtmUser);
        }
        else return Optional.empty();
    }

    @JsonIgnore
    public static Optional<GTMUser> getGTMUser(UUID uuid) {
        return getLoadedUsers().stream().filter( (gtmUser) -> gtmUser.getUuid() == uuid).findFirst();
    }

    @JsonIgnore
    public static void loadUsers() {
        for (Object dataId : Data.getDataList(Data.USER)) {
            GTMUser gtmUser = (GTMUser) Data.obtainData(Data.USER, dataId);
            if (gtmUser == null) {
                System.err.println("Warning! dataId=" + dataId + " contains corrupt data!");
                continue;
            }
            if (gtmUser.getRank().isHigherOrEqualTo(Rank.BUILDTEAM)) {
                gtmUser.updateUserDataIfTime();
            }
            userCache.putIfAbsent((Long) dataId, gtmUser);
        }
        System.out.println("Successfully loaded all " + userCache.size() + " GTM Discord Users!");
    }

    @JsonIgnore
    public static ArrayList<GTMUser> getLoadedUsers() {
        return new ArrayList<>(userCache.values());
    }

    @JsonIgnore
    public static boolean removeGTMUser(long discordId) {
        boolean success = false;

        Optional<GTMUser> optionalGTMUser = GTMUser.getGTMUser(discordId);
        if (optionalGTMUser.isPresent()) {
            userCache.remove(discordId);
            success = Data.deleteData(Data.USER, discordId);
            optionalGTMUser.get().getDiscordMember().ifPresent( member -> {
                // remove donor roles if any
                for (Rank r : Rank.values()) {
                    if (!r.isHigherOrEqualTo(Rank.HELPER) && member.getRoles().contains(r.getRole())) {
                        GTMBot.getGTMGuild().removeRoleFromMember(member.getUser(), r.getRole()).queue();
                    }
                }
                // reset nick
                try {
                    member.modifyNickname("").queue();
                } catch (HierarchyException ex) {
                    System.out.println("[Debug] Unable to update the nick for " + member.getAsMention() + " because they are higher ranked than the bot!");
                }
            });
        }

        return success;
    }

    @JsonIgnore
    public static void saveUser (GTMUser gtmUser) {
        Data.storeData(Data.USER, gtmUser, gtmUser.discordId);
    }

    @JsonIgnore
    public synchronized void updateUserDataNow() {

        if (disableDataUpdates)
            return;

        if (!this.optionalMember.isPresent()) {
            Logs.log("Skipping data update for GTM Player " + this.getUsername() + " because they have left this discord!");
            return;
        }

        this.lastUpdated = System.currentTimeMillis();

        Logs.log("Attempting to update user data for GTM Player " + this.getUsername() + "!");
        long start = System.currentTimeMillis();

        try (Connection conn = BaseDatabase.getInstance(BaseDatabase.Database.USERS).getConnection()) {
            String newUsername = Utils.getUsername(this.getUuid()).orElse(null);
            Rank newRank = DiscordDAO.getRank(conn, this.getUuid());

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
                if (this.rank.isHigherOrEqualTo(Rank.BUILDTEAM) || discordMember.isOwner()) {
                    // msg admins TODO
                } else {
                    // set new role on discord
                    GTMBot.getGTMGuild().addRoleToMember(this.getDiscordMember().get(), rank.getRole()).queue();
                }
            }
            // remove old role(s)
            for (Rank r : Rank.values()) {
                if (r != this.rank && discordMember.getRoles().contains(r.getRole())) {
                    if (rank.isHigherOrEqualTo(Rank.BUILDTEAM) || discordMember.isOwner()) {
                        // msg admins TODO
                    } else if (r.isHigherOrEqualTo(Rank.BUILDTEAM)) {
                        // msg admins TODO
                    } else
                        GTMBot.getGTMGuild().removeRoleFromMember(this.optionalMember.get(), r.getRole()).queue();
                }
            }

            if (!discordMember.getEffectiveName().equals(this.username)) {
                if (!this.rank.isHigherOrEqualTo(Rank.BUILDER) && !discordMember.isOwner()) {
                    discordMember.modifyNickname(username).queue();
                }
            }

            DiscordDAO.updateDiscordTag(conn, this.discordId, discordMember.getUser().getAsTag());

        } catch (SQLException e) {
            Utils.printStackError(e);
        }

        System.out.println("Updated user data for GTM Player " + this.getUsername() + " in " + (System.currentTimeMillis() - start) + " ms!");
    }

    @JsonIgnore
    public void updateUserDataIfTime() {
        if (this.lastUpdated + (UPDATE_TIME * 1000 * 60) < System.currentTimeMillis())
            this.updateUserDataNow();
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
