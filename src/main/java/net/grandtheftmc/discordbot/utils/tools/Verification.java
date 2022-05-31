package net.grandtheftmc.discordbot.utils.tools;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import net.grandtheftmc.discordbot.utils.Utils;
import net.grandtheftmc.discordbot.utils.database.DiscordDAO;
import net.grandtheftmc.discordbot.utils.database.sql.BaseDatabase;
import net.grandtheftmc.discordbot.utils.threads.ThreadUtil;
import net.dv8tion.jda.api.entities.Member;
import org.json.JSONObject;
import net.grandtheftmc.discordbot.utils.Data;
import net.grandtheftmc.discordbot.utils.users.GTMUser;
import net.grandtheftmc.discordbot.utils.users.Rank;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

public class Verification {

    private static final Set<VerifyRequest> verifyRequests = Collections.synchronizedSet(new HashSet<>());

    /** Creates a request to verify a user with GTM
     *
     * @param code - The verification code associated with this user
     * @param uuid - Minecraft UUID of the user
     * @param name - Minecraft user name of the user
     * @param rank - GTM rank of the user
     */
    public synchronized static void createVerifyRequest(String code, UUID uuid, String name, Rank rank) {
        VerifyRequest vr = new VerifyRequest(code, uuid, name, rank);
        verifyRequests.add(vr);
        // Delete request after 15 minutes
        new Timer().schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        verifyRequests.removeIf(vr -> vr.uuid.equals(uuid));
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
    public synchronized static boolean verifyMember(Member m, String code) {
        Optional<VerifyRequest> opVr = verifyRequests.stream().filter(v -> v.verifyCode.equalsIgnoreCase(code)).findFirst();
        if (opVr.isPresent()) {
            VerifyRequest vr = opVr.get();
            GTMUser gtmUser = new GTMUser(vr.uuid, vr.username, vr.rank, m.getIdLong());
            // save to sql database
            try (Connection conn = BaseDatabase.getInstance(BaseDatabase.Database.USERS).getConnection()) {
                DiscordDAO.createDiscordProfile(conn, gtmUser);
                Data.storeData(Data.USER, gtmUser, m.getIdLong());
                // tell gtm verification was successfull
                JSONObject data = new JSONObject()
                        .put("uuid", gtmUser.getUuid())
                        .put("discordId", m.getIdLong())
                        .put("tag", Utils.convertSpecialChar(m.getUser().getAsTag()));
                DiscordDAO.sendToGTM("verified", data);
                gtmUser.updateUserDataNow();
                // remove code
                verifyRequests.removeIf(v -> vr.uuid.equals(gtmUser.getUuid()));
            } catch (SQLException e) {
                Utils.printStackError(e);
                return false;
            }
            return true;
        } else return false;
    }

    public static void unVerifyUser(GTMUser gtmUser) {
        ThreadUtil.runAsync( () -> {
            try (Connection conn = BaseDatabase.getInstance(BaseDatabase.Database.USERS).getConnection()) {
                DiscordDAO.deleteDiscordProfile(conn, gtmUser.getUuid());
            } catch (SQLException e) {
                Utils.printStackError(e);
            }
        });
        DiscordDAO.sendToGTM("unverify", new JSONObject().put("uuid", gtmUser.getUuid()));
    }

    @Getter @AllArgsConstructor @ToString
    public static class VerifyRequest {
        String verifyCode;
        UUID uuid;
        String username;
        Rank rank;
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof VerifyRequest))
                return false;
            VerifyRequest vo = (VerifyRequest) o;
            return vo.uuid.equals(this.uuid);
        }
        @Override
        public int hashCode() {
            return this.uuid.hashCode();
        }
    }

}
