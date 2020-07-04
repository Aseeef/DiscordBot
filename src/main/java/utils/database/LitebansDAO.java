package utils.database;

import me.kbrewster.exceptions.APIException;
import me.kbrewster.mojangapi.MojangAPI;
import utils.litebans.Ban;
import utils.tools.GTools;

import java.io.IOException;
import java.sql.*;
import java.util.UUID;

public class LitebansDAO {

    public static Ban getBanByPlayer (Connection conn, String player) {
        try {
            UUID uuid = MojangAPI.getUUID(player);
            String query = "SELECT * FROM `litebans_bans` WHERE `uuid`=? AND `active`=1;";

            try (PreparedStatement statement = conn.prepareStatement(query)) {
                statement.setString(1, uuid.toString());

                try (ResultSet result = statement.executeQuery()) {
                    if (result.next()) {
                        int id = result.getInt("id");
                        String ip = result.getString("ip");
                        String reason = result.getString("reason");
                        String banUuidString = result.getString("banned_by_uuid");
                        UUID banUuid = banUuidString == null ? null : UUID.fromString(banUuidString);
                        String banName = result.getString("banned_by_name");
                        String unbanUuidString = result.getString("removed_by_uuid");
                        UUID unbanUuid = unbanUuidString == null ? null : UUID.fromString(unbanUuidString);
                        String unbanName = result.getString("removed_by_name");
                        Timestamp unbanTime = result.getTimestamp("removed_by_date");
                        long banTime = result.getLong("time");
                        long expiry = result.getLong("until");
                        String serverScope = result.getString("server_scope");
                        String serverOrigin = result.getString("server_origin");
                        boolean silent = result.getBoolean("silent");
                        boolean ipBan = result.getBoolean("ipban");
                        boolean active = result.getBoolean("active");
                        return new Ban(id, uuid, ip, reason, banUuid, banName, unbanUuid, unbanName, unbanTime, banTime, expiry, serverScope, serverOrigin, silent, ipBan, active);
                    }
                }
            }
        } catch (SQLException | APIException | IOException e) {
            GTools.printStackError(e);
        }

        return null;
    }

}
