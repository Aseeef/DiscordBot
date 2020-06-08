package Utils.database.redis;

import Utils.Rank;
import Utils.tools.GTools;
import Utils.tools.Logs;
import Utils.tools.Verification;
import net.grandtheftmc.jedisnew.RedisEventListener;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

public class OnRedisMessageReceive implements RedisEventListener {

    @Override
    public String getChannel() {
        return "gtm_to_discord";
    }

    @Override
    public void onRedisEvent(String s, JSONObject jsonObject) {
        try {

            String action = jsonObject.getString("action");

            Logs.log("[DEBUG] [DatabaseEvent] Received the following data from type " + action + " from GTM!");
            Logs.log("[DEBUG] [DatabaseEvent] " + jsonObject.toString());

            switch (action.toLowerCase()) {

                case "verify":
                    UUID uuid = UUID.fromString(jsonObject.getString("uuid"));
                    String name = jsonObject.getString("name");
                    Rank rank = Rank.getRankFromString(jsonObject.getString("rank"));
                    String verifyCode = jsonObject.getString("code");
                    if (rank == null) return;
                    Verification.createVerifyRequest(verifyCode, uuid, name, rank);
                    break;

                case "disconnect":

                    break;

                default:
                    Logs.log("[DEBUG] [DatabaseEvent] '" + action + "' is an unknown action!", Logs.WARNING);
                    break;


            }
        } catch (JSONException e) {
            GTools.printStackError(e);
        }

    }

}
