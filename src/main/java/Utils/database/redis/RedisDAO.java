package Utils.database.redis;

import Utils.database.BaseDatabase;
import org.json.JSONObject;
import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.Map;

public class RedisDAO {

    public static void sendToGTM(String action, HashMap<String, Object> data) {
        data.put("action", action);
        String serializedData = serialize(data);
        try (Jedis jedis = BaseDatabase.getRedisInstance().getResource()) {
            jedis.publish("discord_to_gtm", serializedData);
        }
    }

    public static String serialize(Map<String, Object> data) {
        JSONObject jsonObject = new JSONObject();
        data.keySet().forEach(key -> jsonObject.put(key, data.get(key)));
        return jsonObject.toString();
    }

}
