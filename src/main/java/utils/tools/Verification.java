package utils.tools;

import org.json.JSONObject;
import utils.Data;
import utils.Rank;
import utils.database.DiscordDAO;
import utils.database.sql.BaseDatabase;
import utils.users.GTMUser;
import net.dv8tion.jda.api.entities.Member;
import static utils.database.DiscordDAO.createDiscordProfile;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

public class Verification {

    private static HashMap<String, Map<String, Object>> verifyHashMap = new HashMap<>();

    /** Creates a request to verify a user with GTM
     *
     * @param code - The verification code associated with this user
     * @param uuid - Minecraft UUID of the user
     * @param name - Minecraft user name of the user
     * @param rank - GTM rank of the user
     */
    public static void createVerifyRequest(String code, UUID uuid, String name, Rank rank) {
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("uuid", uuid);
        dataMap.put("name", name);
        dataMap.put("rank", rank);
        verifyHashMap.put(code, dataMap);
        // Delete request after 15 minutes
        new Timer().schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        verifyHashMap.remove(code);
                    }
                },
                1000 * 60 * 15
        );
    }

    /** Attempts to verify a discord member with the provided code
     *
     * @param m - The discord member who to attempt to verify
     * @param code - The entered verification code which to match to a user
     * @return - Whether verification was successful or not (which depends on whether the code was correct or not)
     */
    public static boolean verifyMember(Member m, String code) {
        if (verifyHashMap.containsKey(code)) {
            Map<String, Object> dataMap = verifyHashMap.get(code);
            GTMUser gtmUser = new GTMUser((UUID) dataMap.get("uuid"), (String) dataMap.get("name"), (Rank) dataMap.get("rank"), m.getIdLong());
            // save to sql database
            try (Connection conn = BaseDatabase.getInstance(BaseDatabase.Database.USERS).getConnection()) {
                createDiscordProfile(conn, gtmUser);
            } catch (SQLException e) {
                GTools.printStackError(e);
                return false;
            }
            Data.storeData(Data.USER, gtmUser, m.getIdLong());
            // tell gtm verification was successfull
            JSONObject data = new JSONObject()
                    .put("uuid", gtmUser.getUuid())
                    .put("discordId", m.getIdLong())
                    .put("tag", GTools.convertSpecialChar(m.getUser().getAsTag()));
            DiscordDAO.sendToGTM("verified", data);
            gtmUser.updateUserDataNow();
            // remove code
            verifyHashMap.remove(code);
            return true;
        } else return false;
    }

    public static void unVerifyUser(GTMUser gtmUser) {
        GTools.runAsync( () -> {
            try (Connection conn = BaseDatabase.getInstance(BaseDatabase.Database.USERS).getConnection()) {
                DiscordDAO.deleteDiscordProfile(conn, gtmUser.getUuid());
            } catch (SQLException e) {
                GTools.printStackError(e);
            }
        });
        DiscordDAO.sendToGTM("unverify", new JSONObject().put("uuid", gtmUser.getUuid()));
    }

}
