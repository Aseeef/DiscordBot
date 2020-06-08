package Utils.database;

import Utils.Rank;
import Utils.database.sql.BaseDatabase;
import Utils.tools.GTools;
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

}
