package Utils.database;

import Utils.Rank;
import Utils.database.sql.BaseDatabase;
import Utils.tools.GTools;
import Utils.tools.UUIDUtil;
import Utils.users.GTMUser;
import me.kbrewster.exceptions.APIException;
import me.kbrewster.mojangapi.MojangAPI;
import net.grandtheftmc.jedisnew.NewJedisManager;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static Utils.tools.GTools.jedisManager;

public class DAO {

    public static void sendToGTM(String action, HashMap<String, Object> data) {
        data.put("action", action);
        jedisManager.sendData("discord_to_gtm", NewJedisManager.serialize(data));
    }

    public static void sendToGTM(String action, JSONObject data) {
        data.put("action", action);
        jedisManager.sendData("discord_to_gtm", data);
    }

    public static String getSkullSkin (UUID uuid) {
        String stringUUID = uuid.toString().replace("-", "");
        return "https://minotar.net/avatar/" + stringUUID + ".png";
    }

    public static Optional<String> getUsername (UUID uuid) {
        try {
            return Optional.of(MojangAPI.getUsername(uuid));
        } catch (IOException | APIException e) {
            GTools.printStackError(e);
        }
        return Optional.empty();
    }

    public static Optional<UUID> getUUID(String userName) {
        try {
            return Optional.of(MojangAPI.getUUID(userName));
        } catch (IOException | APIException e) {
            GTools.printStackError(e);
        }
        return Optional.empty();
    }

    public static Rank getRank(UUID uuid) {

        try (Connection conn = BaseDatabase.getInstance(BaseDatabase.Database.USERS).getConnection()) {

            String query = "SELECT * FROM `user_profile` WHERE `uuid`=UNHEX(?)";

            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setString(1, uuid.toString().replaceAll("-", ""));
                try (ResultSet result = ps.executeQuery()) {
                    if (result.next()) {
                        String stringRank = result.getString("rank");
                        return Rank.getRankFromString(stringRank);
                    }
                }
            } catch (Exception e) {
                GTools.printStackError(e);
            }
        } catch (SQLException e) {
            GTools.printStackError(e);
        }

        return null;
    }

    public static void createDiscordProfile(GTMUser gtmUser) {

        try (Connection conn = BaseDatabase.getInstance(BaseDatabase.Database.USERS).getConnection()) {

            String query = "INSERT INTO `discord_users` (`uuid`, `discord_tag`, `discord_id`) VALUES (UNHEX(?),?,?);";

            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setString(1, gtmUser.getUuid().toString().replace("-", ""));
                ps.setString(2, gtmUser.getDiscordMember().getUser().getAsTag().replaceFirst("@", ""));
                ps.setLong(3, gtmUser.getDiscordId());

                ps.executeUpdate();
            }

        } catch (Exception e) {
            GTools.printStackError(e);
        }

    }

    public static void deleteDiscordProfile(UUID uuid) {

        try (Connection conn = BaseDatabase.getInstance(BaseDatabase.Database.USERS).getConnection()) {

            String query = "SELECT * FROM discord_users WHERE uuid=UNHEX(?);";

            try (PreparedStatement statement = conn.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
                statement.setString(1, uuid.toString().replaceAll("-", ""));
                try (ResultSet result = statement.executeQuery()) {
                    if (result.next()) {
                        result.deleteRow();
                    }
                }
            }

        } catch (SQLException e) {
            GTools.printStackError(e);
        }

    }

    /**
     * @deprecated - Use with caution. If a user rank has too many players, may freeze. Only use for staff ranks.
     */
    @Deprecated
    public static List<GTMUser> getAllWithRank(Rank rank) {

        List<GTMUser> gtmUsersWithRank = new ArrayList<>();

        try (Connection conn = BaseDatabase.getInstance(BaseDatabase.Database.USERS).getConnection()) {

            String query = "SELECT HEX(uuid) FROM user_profile WHERE rank=?;";

            try (PreparedStatement statement = conn.prepareStatement(query)) {
                statement.setString(1, rank.n());
                try (ResultSet result = statement.executeQuery()) {
                    while (result.next()) {
                        String hexStringUUID = result.getString("HEX(uuid)");
                        UUID uuid = UUIDUtil.createUUID(hexStringUUID).orElse(null);
                        if (uuid != null) {
                            GTMUser gtmUser = GTMUser.getGTMUser(DAO.getDiscordIdFromUUID(uuid)).orElse(null);
                            if (gtmUser != null) gtmUsersWithRank.add(gtmUser);
                        }
                    }
                }
            }

            return gtmUsersWithRank;

        } catch (SQLException e) {
            GTools.printStackError(e);
        }

        return null;

    }

    public static long getDiscordIdFromName (String username) {
        UUID uuid = getUUID(username).orElse(null);
        if (uuid == null) return -1;
        return getDiscordIdFromUUID(uuid);
    }

    public static long getDiscordIdFromUUID(UUID uuid) {

        try (Connection conn = BaseDatabase.getInstance(BaseDatabase.Database.USERS).getConnection()) {

            String query = "SELECT * FROM discord_users WHERE uuid=UNHEX(?);";

            try (PreparedStatement statement = conn.prepareStatement(query)) {
                statement.setString(1, uuid.toString().replace("-", ""));
                try (ResultSet result = statement.executeQuery()) {
                    if (result.next()) {
                        return result.getLong("discord_id");
                    }
                }
            }

        } catch (SQLException e) {
            GTools.printStackError(e);
        }

        return -1;

    }

    public static boolean discordProfileExists(long discordId) {

        try (Connection conn = BaseDatabase.getInstance(BaseDatabase.Database.USERS).getConnection()) {

            String query = "SELECT * FROM discord_users WHERE discord_id=?;";

            try (PreparedStatement statement = conn.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
                statement.setLong(1, discordId);
                try (ResultSet result = statement.executeQuery()) {
                    if (result.next()) {
                        return true;
                    }
                }
            }

        } catch (SQLException e) {
            GTools.printStackError(e);
        }

        return false;

    }

}
