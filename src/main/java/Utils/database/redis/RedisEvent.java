package Utils.database.redis;

import Utils.Rank;
import Utils.tools.Logs;
import Utils.tools.Verification;
import org.json.JSONObject;
import redis.clients.jedis.JedisPubSub;

import java.util.UUID;
public class RedisEvent extends JedisPubSub {

    private static RedisEvent instance;

    public RedisEvent() {
        instance = this;
    }

    public static RedisEvent getInstance() {
        return instance;
    }

    @Override
    public void onMessage(String channel, String message) {

        JSONObject jsonData = new JSONObject(message);
        String action = jsonData.getString("action");

        switch (action.toLowerCase()) {

            case "verify":
                UUID uuid = UUID.fromString(jsonData.getString("uuid"));
                String name = jsonData.getString("name");
                Rank rank = Rank.getRankFromString(jsonData.getString("rank"));
                String verifyCode = jsonData.getString("code");
                if (rank == null) return;
                Verification.createVerifyRequest(verifyCode, uuid, name, rank);
                break;

            case "disconnect":

                break;

            default:
                Logs.log("[DEBUG] [DatabaseEvent] Received unknown action request from GTM: " + action);
                break;


        }

    }

}
