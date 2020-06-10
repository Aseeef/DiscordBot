package Utils.database;

import Utils.Rank;
import Utils.database.sql.BaseDatabase;
import Utils.tools.GTools;
import Utils.users.GTMUser;
import com.fasterxml.jackson.databind.ser.Serializers;
import me.kbrewster.exceptions.APIException;
import me.kbrewster.mojangapi.MojangAPI;
import net.grandtheftmc.jedis.JedisManager;
import net.grandtheftmc.jedisnew.NewJedisManager;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

import static Utils.tools.GTools.jedisManager;

public class DAO {

    public static void sendToGTM(String action, HashMap<String, Object> data) {
        data.put("action", action);
        jedisManager.sendData("discord_to_gtm", NewJedisManager.serialize(data));
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
