package net.grandtheftmc.discordbot.commands.message;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.grandtheftmc.discordbot.commands.stats.PlanServer;
import net.grandtheftmc.discordbot.utils.database.sql.BaseDatabase;
import net.grandtheftmc.discordbot.utils.users.GTMUser;
import net.grandtheftmc.discordbot.utils.users.Rank;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.List;

@AllArgsConstructor @Getter @EqualsAndHashCode
public class ConditionalMessage {

    private Member author;
    private String title;
    private String content;
    private String hexColor;
    private Set<MessageCondition> conditionOptions;

    public void sendMessage() {
        MessageEmbed embed = new EmbedBuilder()
                .setTitle(title)
                .setAuthor(author.getEffectiveName())
                .setImage(author.getAvatarUrl())
                .setDescription(content)
                .setColor(Color.decode(hexColor))
                .build();


        for (GTMUser gtmUser : compileSendUsers()) {
            try {
                Optional<User> opUser = gtmUser.getUser();
                if (opUser.isPresent()) {
                    opUser.get().openPrivateChannel().queue(pc -> {
                        pc.sendMessageEmbeds(embed).complete();
                    });
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * @return a list of all GTMUsers who should receive this conditional message based of the conditions
     */
    public List<GTMUser> compileSendUsers() {
        ArrayList<GTMUser> gtmUsers = new ArrayList<>();

        // go through each condition adding GTMUsers to it
        for (MessageCondition condition : conditionOptions) {

            switch (condition.option) {

                case MONEY:
                    try (Connection conn = BaseDatabase.getInstance(BaseDatabase.Database.USERS).getConnection()) {
                        handleMoney(conn, condition, gtmUsers);
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                    break;

                case RANK:
                    try (Connection conn = BaseDatabase.getInstance(BaseDatabase.Database.USERS).getConnection()) {
                        handleRank(conn, condition, gtmUsers);
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                    break;

                case LEVEL:
                    try (Connection conn = BaseDatabase.getInstance(BaseDatabase.Database.USERS).getConnection()) {
                        handleLevel(conn, condition, gtmUsers);
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                    break;
                case PLAYTIME:
                    try (Connection conn = BaseDatabase.getInstance(BaseDatabase.Database.USERS).getConnection()) {
                        handlePlaytime(conn, condition, gtmUsers);
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                    break;
                case LAST_PLAYED:
                    try (Connection conn = BaseDatabase.getInstance(BaseDatabase.Database.PLAN).getConnection()) {
                        handleLastPlayed(conn, condition, gtmUsers);
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                    break;
                default:
                    throw new IllegalStateException("Unknown message condition '" + condition + "'!");
            }

        }

        return gtmUsers;

    }

    public static void handleMoney(Connection conn, MessageCondition condition, ArrayList<GTMUser> listToModify) throws SQLException {
        String query = "SELECT discord_users.discord_id,SUM(user_currency.amount) as 'money' FROM user_currency INNER JOIN discord_users ON user_currency.uuid=discord_users.uuid WHERE discord_id != -1 " + (condition.targetServer == null ? "" : "AND user_currency.server_key=? ") + "AND (currency='BANK' OR currency='MONEY') GROUP BY user_currency.uuid;";

        try (PreparedStatement ps = conn.prepareStatement(query)) {

            // if null, then we do global money
            if (condition.targetServer != null)
                ps.setString(1, condition.targetServer.toString().toUpperCase());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long money = rs.getLong("money");
                    long discordId = rs.getLong("discord_id");
                    switch (condition.getType()) {
                        case EQUAL_TO:
                            if (money == (long) condition.value) {
                                GTMUser.getGTMUser(discordId).ifPresent(listToModify::add);
                            }
                            break;
                        case LESS_THAN:
                            if (money < (long) condition.value) {
                                GTMUser.getGTMUser(discordId).ifPresent(listToModify::add);
                            }
                            break;
                        case LESS_THAN_OR_EQUAL_TO:
                            if (money <= (long) condition.value) {
                                GTMUser.getGTMUser(discordId).ifPresent(listToModify::add);
                            }
                            break;
                        case GREATER_THAN:
                            if (money > (long) condition.value) {
                                GTMUser.getGTMUser(discordId).ifPresent(listToModify::add);
                            }
                            break;
                        case GREATER_THAN_OR_EQUAL_TO:
                            if (money >= (long) condition.value) {
                                GTMUser.getGTMUser(discordId).ifPresent(listToModify::add);
                            }
                            break;
                    }
                }
            }
        }
    }

    public static void handleRank(Connection conn, MessageCondition condition, ArrayList<GTMUser> listToModify) throws SQLException {
        String query = "SELECT discord_users.discord_id,user_profile.`rank` FROM user_profile INNER JOIN discord_users ON user_profile.uuid=discord_users.uuid WHERE discord_id != -1 AND (user_profile.server_key='GLOBAL'" + (condition.targetServer == null ? "" : " OR user_profile.server_key=?") + ");";

        try (PreparedStatement ps = conn.prepareStatement(query)) {

            // if null, then we ONLY consider global ranks
            if (condition.targetServer != null)
                ps.setString(1, condition.targetServer.toString().toUpperCase());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long discordId = rs.getLong("discord_id");
                    Rank rank = Rank.getRankFromString(rs.getString("rank"));
                    Rank targetRank = Rank.getRankFromString((String) condition.value);
                    switch (condition.getType()) {
                        case EQUAL_TO:
                            if (rank == targetRank) {
                                GTMUser.getGTMUser(discordId).ifPresent(listToModify::add);
                            }
                            break;
                        case LESS_THAN:
                            if (targetRank.isHigher(rank)) {
                                GTMUser.getGTMUser(discordId).ifPresent(listToModify::add);
                            }
                            break;
                        case LESS_THAN_OR_EQUAL_TO:
                            if (targetRank.isHigherOrEqualTo(rank)) {
                                GTMUser.getGTMUser(discordId).ifPresent(listToModify::add);
                            }
                            break;
                        case GREATER_THAN:
                            if (rank.isHigher(targetRank)) {
                                GTMUser.getGTMUser(discordId).ifPresent(listToModify::add);
                            }
                            break;
                        case GREATER_THAN_OR_EQUAL_TO:
                            if (rank.isHigherOrEqualTo(targetRank)) {
                                GTMUser.getGTMUser(discordId).ifPresent(listToModify::add);
                            }
                            break;
                    }
                }
            }
        }
    }

    public static void handleLevel(Connection conn, MessageCondition condition, ArrayList<GTMUser> listToModify) throws SQLException {
        String query = "SELECT ?.level,discord_users.discord_id FROM ? INNER JOIN discord_users ON ?.uuid=discord_users.uuid WHERE discord_id != -1;";

        //todo:
        if (!condition.targetServer.toString().toLowerCase().startsWith("gtm"))
            throw new IllegalArgumentException("Only GTM servers may have the level condition!");

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, condition.targetServer.toString().toLowerCase());
            ps.setString(2, condition.targetServer.toString().toLowerCase());
            ps.setString(3, condition.targetServer.toString().toLowerCase());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long discordId = rs.getLong("discord_id");
                    int level = rs.getInt("level");
                    switch (condition.getType()) {
                        case EQUAL_TO:
                            if (level == (int) condition.value) {
                                GTMUser.getGTMUser(discordId).ifPresent(listToModify::add);
                            }
                            break;
                        case LESS_THAN:
                            if (level < (int) condition.value) {
                                GTMUser.getGTMUser(discordId).ifPresent(listToModify::add);
                            }
                            break;
                        case LESS_THAN_OR_EQUAL_TO:
                            if (level <= (int) condition.value) {
                                GTMUser.getGTMUser(discordId).ifPresent(listToModify::add);
                            }
                            break;
                        case GREATER_THAN:
                            if (level > (int) condition.value) {
                                GTMUser.getGTMUser(discordId).ifPresent(listToModify::add);
                            }
                            break;
                        case GREATER_THAN_OR_EQUAL_TO:
                            if (level >= (int) condition.value) {
                                GTMUser.getGTMUser(discordId).ifPresent(listToModify::add);
                            }
                            break;
                    }
                }
            }
        }
    }

    public static void handlePlaytime(Connection conn, MessageCondition condition, ArrayList<GTMUser> listToModify) throws SQLException {
        String query = "SELECT ?.playtime,discord_users.discord_id FROM ? INNER JOIN discord_users ON ?.uuid=discord_users.uuid WHERE discord_id != -1;";

        //todo:
        if (!condition.targetServer.toString().toLowerCase().startsWith("gtm"))
            throw new IllegalArgumentException("Only GTM servers store playtime data!");

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, condition.targetServer.toString().toLowerCase());
            ps.setString(2, condition.targetServer.toString().toLowerCase());
            ps.setString(3, condition.targetServer.toString().toLowerCase());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long discordId = rs.getLong("discord_id");
                    int playtimeHours = Math.round(rs.getInt("playtime") / 20f / 60f);
                    switch (condition.getType()) {
                        case EQUAL_TO:
                            if (playtimeHours == (int) condition.value) {
                                GTMUser.getGTMUser(discordId).ifPresent(listToModify::add);
                            }
                            break;
                        case LESS_THAN:
                            if (playtimeHours < (int) condition.value) {
                                GTMUser.getGTMUser(discordId).ifPresent(listToModify::add);
                            }
                            break;
                        case LESS_THAN_OR_EQUAL_TO:
                            if (playtimeHours <= (int) condition.value) {
                                GTMUser.getGTMUser(discordId).ifPresent(listToModify::add);
                            }
                            break;
                        case GREATER_THAN:
                            if (playtimeHours > (int) condition.value) {
                                GTMUser.getGTMUser(discordId).ifPresent(listToModify::add);
                            }
                            break;
                        case GREATER_THAN_OR_EQUAL_TO:
                            if (playtimeHours >= (int) condition.value) {
                                GTMUser.getGTMUser(discordId).ifPresent(listToModify::add);
                            }
                            break;
                    }
                }
            }
        }
    }

    // this is the most inefficient one because we have to get this data through plan
    // and i cant inner join discord tables...
    public static void handleLastPlayed(Connection conn, MessageCondition condition, ArrayList<GTMUser> listToModify) throws SQLException {
        String query = "SELECT plan_users.uuid, MAX(session_end) as 'last_seen' FROM plan_sessions INNER JOIN plan_users ON plan_sessions.user_id = plan_users.id WHERE " + (condition.targetServer == null ? "" : "server_id=? ") + "GROUP BY plan_sessions.id;";

        try (PreparedStatement ps = conn.prepareStatement(query)) {

            // if target server is null, then we consider last played over ALL servers
            if (condition.targetServer != null)
                ps.setInt(1, condition.targetServer.getId());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    UUID uuid = UUID.fromString(rs.getString("uuid"));
                    long lastSeen = rs.getLong("last_seen");
                    switch (condition.getType()) {
                        case EQUAL_TO:
                            if (lastSeen == (long) condition.value) {
                                GTMUser.getGTMUser(uuid).ifPresent(listToModify::add);
                            }
                            break;
                        case LESS_THAN:
                            if (lastSeen < (long) condition.value) {
                                GTMUser.getGTMUser(uuid).ifPresent(listToModify::add);
                            }
                            break;
                        case LESS_THAN_OR_EQUAL_TO:
                            if (lastSeen <= (long) condition.value) {
                                GTMUser.getGTMUser(uuid).ifPresent(listToModify::add);
                            }
                            break;
                        case GREATER_THAN:
                            if (lastSeen > (long) condition.value) {
                                GTMUser.getGTMUser(uuid).ifPresent(listToModify::add);
                            }
                            break;
                        case GREATER_THAN_OR_EQUAL_TO:
                            if (lastSeen >= (long) condition.value) {
                                GTMUser.getGTMUser(uuid).ifPresent(listToModify::add);
                            }
                            break;
                    }
                }
            }
        }
    }

    @Getter @AllArgsConstructor @EqualsAndHashCode
    public static class MessageCondition {
        @NotNull private ConditionalOption option;
        @NotNull private ConditionType type;
        @NotNull private Object value;
        @Nullable private PlanServer targetServer;
    }

}
