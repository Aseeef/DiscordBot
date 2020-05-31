package Utils.tools;

import Utils.Data;
import Utils.Rank;
import Utils.users.GTMUser;
import net.dv8tion.jda.api.entities.Member;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
            GTMUser GTMUser = new GTMUser((UUID) dataMap.get("uuid"), (String) dataMap.get("name"), (Rank) dataMap.get("rank"), m.getIdLong());
            Data.storeData(Data.USER, GTMUser, m.getIdLong());
            return true;
        } else return false;
    }

}
