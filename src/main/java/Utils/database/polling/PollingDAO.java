package Utils.database.polling;

import Database.BaseDatabase;
import Utils.tools.GTools;
import Utils.tools.Logs;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class PollingDAO {

    public static void poll(Connection conn) {

        if (newDataExists(conn)) {

            String query = "SELECT * FROM gtm_to_discord;";

            try (PreparedStatement ps = conn.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
                try (ResultSet result = ps.executeQuery()) {
                    while (result.next()) {
                        UUID uuid = null;
                        String stringUUID = result.getString("uuid");
                        if (stringUUID != null) uuid = UUID.fromString(stringUUID);
                        String name = result.getString("username");
                        String action = result.getString("action");
                        String data = result.getString("data");

                        result.deleteRow();
                        Logs.log("[DEBUG] [PollingDAO] Received incoming data from GTM of action " + action + "!");
                        new DatabaseEvent(uuid, name, action, data);
                    }
                }
            } catch (SQLException e) {
                GTools.printStackError(e);
            }
        }
    }

    public static boolean newDataExists(Connection conn) {

        String query = "SELECT COUNT(*) FROM gtm_to_discord;";

        try (PreparedStatement ps = conn.prepareStatement(query)){
            try (ResultSet result = ps.executeQuery()) {
                if (result.next()) {
                    int recordCount = result.getInt("COUNT(*)");
                    if (recordCount > 0) {
                        return true;
                    }
                }
            }
        }
        catch (SQLException e) {
            GTools.printStackError(e);
        }

        return false;
    }

}
