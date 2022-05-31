package net.grandtheftmc.discordbot.utils.database;

import net.grandtheftmc.discordbot.GTMBot;
import net.grandtheftmc.discordbot.utils.Utils;
import net.grandtheftmc.discordbot.utils.database.sql.BaseDatabase;
import net.grandtheftmc.simplejedis.SimpleJedisManager;
import org.json.JSONObject;
import net.grandtheftmc.discordbot.utils.UUIDUtil;
import net.grandtheftmc.discordbot.utils.users.GTMUser;
import net.grandtheftmc.discordbot.utils.users.Rank;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class DiscordDAO {

    public static void sendToGTM(String action, HashMap<String, Object> data) {
        data.put("action", action);
        GTMBot.getJedisManager().sendData("discord_to_gtm", SimpleJedisManager.serialize(data));
    }

    public static void sendToGTM(String action, JSONObject data) {
        data.put("action", action);
        GTMBot.getJedisManager().sendData("discord_to_gtm", data);
    }

    public static void sendToBungee(String action, HashMap<String, Object> data) {
        data.put("action", action);
        GTMBot.getJedisManager().sendData("discord_to_bungee", SimpleJedisManager.serialize(data));
    }

    public static void sendToBungee(String action, JSONObject data) {
        data.put("action", action);
        GTMBot.getJedisManager().sendData("discord_to_bungee", data);
    }

    public static Rank getRank (UUID uuid) {
        try (Connection conn = BaseDatabase.getInstance(BaseDatabase.Database.USERS).getConnection()) {
            return getRank(conn, uuid);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static Rank getRank(Connection conn, UUID uuid) {
        String query = "SELECT * FROM `user_profile` WHERE `uuid`=UNHEX(?) AND `server_key`='GLOBAL'";

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, uuid.toString().replaceAll("-", ""));
            try (ResultSet result = ps.executeQuery()) {
                if (result.next()) {
                    String stringRank = result.getString("rank");
                    return Rank.getRankFromString(stringRank);
                }
            }
        } catch (Exception e) {
            Utils.printStackError(e);
        }

        return null;
    }

    public static void createDiscordProfile(Connection conn, GTMUser gtmUser) throws SQLException {
        String mention = gtmUser.getUser().get().getAsTag();
        mention = mention.replaceFirst("@", "");
        mention = Utils.convertSpecialChar(mention); //changes character encoding to something accepted by database

        String query = "INSERT INTO `discord_users` (`uuid`, `discord_tag`, `discord_id`, `verify_active`, `verify_time`) VALUES (UNHEX(?),?,?,?,?) ON DUPLICATE KEY UPDATE `discord_tag`=?, `discord_id`=?, `verify_active`=?, `verify_time`=?;";

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, gtmUser.getUuid().toString().replace("-", ""));

            ps.setString(2, mention);
            ps.setLong(3, gtmUser.getDiscordId());
            ps.setBoolean(4, true);
            ps.setTimestamp(5, new Timestamp(System.currentTimeMillis()));

            ps.setString(6, mention);
            ps.setLong(7, gtmUser.getDiscordId());
            ps.setBoolean(8, true);
            ps.setTimestamp(9, new Timestamp(System.currentTimeMillis()));

            ps.executeUpdate();
        }

    }

    public static void updateDiscordTag(Connection conn, long discordId, String tag) {
        tag = tag.replaceFirst("@", "");
        tag = Utils.convertSpecialChar(tag); //changes character encoding to something accepted by database

        if (!discordProfileExists(conn, discordId)) return;

        String query = "UPDATE `discord_users` SET `discord_tag`=? WHERE `discord_id`=?;";

        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, tag);
            ps.setLong(2, discordId);
            ps.executeUpdate();
        } catch (Exception e) {
            Utils.printStackError(e);
        }
    }

    public static void deleteDiscordProfile(Connection conn, UUID uuid) {
        String query = "UPDATE `discord_users` SET `verify_active` = ? WHERE uuid=UNHEX(?);";

        try (PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setBoolean(1, false);
            statement.setString(2, uuid.toString().replaceAll("-", ""));
            statement.executeUpdate();
        } catch (SQLException e) {
            Utils.printStackError(e);
        }

    }

    /**
     * @deprecated - Use with caution. If a user rank has too many players, may freeze. Only use for staff ranks.
     */
    @Deprecated
    public static List<GTMUser> getAllWithRank(Connection conn, Rank rank) {

        List<GTMUser> gtmUsersWithRank = new ArrayList<>();

        String query = "SELECT HEX(uuid) FROM user_profile WHERE rank=? AND `verify_active`=1;";

        try (PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setString(1, rank.n());
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    String hexStringUUID = result.getString("HEX(uuid)");
                    UUID uuid = UUIDUtil.createUUID(hexStringUUID).orElse(null);
                    if (uuid != null) {
                        GTMUser gtmUser = GTMUser.getGTMUser(DiscordDAO.getDiscordIdFromUUID(conn, uuid)).orElse(null);
                        if (gtmUser != null) gtmUsersWithRank.add(gtmUser);
                    }
                }
            }
        } catch (SQLException e) {
            Utils.printStackError(e);
        }

        return gtmUsersWithRank;

    }

    public static long getDiscordIdFromName (Connection conn, String username) {
        UUID uuid = Utils.getUUID(username).orElse(null);
        if (uuid == null) return -1;
        return getDiscordIdFromUUID(conn, uuid);
    }

    public static long getDiscordIdFromUUID(Connection conn, UUID uuid) {

        String query = "SELECT * FROM discord_users WHERE uuid=UNHEX(?) AND `verify_active`=1;";

        try (PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setString(1, uuid.toString().replace("-", ""));
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return result.getLong("discord_id");
                }
            }
        } catch (SQLException e) {
            Utils.printStackError(e);
        }

        return -1;

    }

    public static boolean discordProfileExists(Connection conn, long discordId) {

            String query = "SELECT * FROM `discord_users` WHERE discord_id=? AND `verify_active`=1;";

            try (PreparedStatement statement = conn.prepareStatement(query)) {
                statement.setLong(1, discordId);
                try (ResultSet result = statement.executeQuery()) {
                    if (result.next()) {
                        return true;
                    }
                }
            } catch (SQLException e) {
                Utils.printStackError(e);
            }

        return false;

    }

}
