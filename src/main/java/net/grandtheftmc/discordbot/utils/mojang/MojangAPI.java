package net.grandtheftmc.discordbot.utils.mojang;

import net.grandtheftmc.discordbot.utils.UUIDUtil;
import net.grandtheftmc.discordbot.utils.Utils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONObject;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * A fast MojangAPI lookup took that utilizes a small cache to boost performance and reduce api hits
 *
 * @author aseef (aka SkylixMC)
 */
public class MojangAPI {

    private static MojangAPI INSTANCE = null;

    private final ExecutorService service = Executors.newFixedThreadPool(2);
    private final int MAX_CACHE_SIZE = 5000; // cache no more than 5000 profiles
    private final long MAX_CACHE_TIME = 1000L * 60 * 60 * 3; // cache for up to 6 hours
    private final long RATE_LIMIT_TRY_AGAIN = 1000L; // if rate limited, how long to retry after
    private final int MAX_ATTEMPTS = 10; // max attempts to make if we run into an error

    private final Map<UUID, MojangProfile> PROFILE_CACHE = Collections.synchronizedMap(new LinkedHashMap<UUID, MojangProfile>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry eldest) {
            return this.size() > MAX_CACHE_SIZE;
        }
    });

    private MojangAPI() {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(() -> {
            synchronized (PROFILE_CACHE) {
                Iterator<Map.Entry<UUID, MojangProfile>> i = PROFILE_CACHE.entrySet().iterator();
                while (i.hasNext()) {
                    if (i.next().getValue().created < System.currentTimeMillis() - MAX_CACHE_TIME) {
                        i.remove();
                    }
                }
            }
        }, 0, Math.max(MAX_CACHE_TIME / 60, 1000), TimeUnit.MILLISECONDS);
    }

    public static MojangAPI getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new MojangAPI();
        }
        return INSTANCE;
    }

    public String getUsername(UUID uuid) throws IOException {
        return getUsername(uuid, false);
    }

    public UUID getUUID(String username) throws IOException {
        return getUUID(username, false);
    }

    public MojangProfile getProfile(UUID uuid) throws IOException {
        return getProfile(uuid, false);
    }

    private final HashSet<String> lookingUp = new HashSet<>();
    public UUID getUUID(String username, boolean ignoreCache) throws IOException {
        if (!ignoreCache) {
            Optional<MojangProfile> optProfile = PROFILE_CACHE.values().stream().filter(u -> username.equals(u.getUsername())).findFirst();
            if (optProfile.isPresent()) {
                return optProfile.get().getUuid();
            }
        }

        String uuidToUsername = "https://api.mojang.com/users/profiles/minecraft/%s";
        uuidToUsername = String.format(uuidToUsername, username);

        int currentAttempts = 0;
        String errorMessage;
        do {
            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                HttpGet get = new HttpGet(uuidToUsername);
                CloseableHttpResponse response = httpClient.execute(get);
                HttpEntity responseEntity = response.getEntity();
                if (responseEntity == null)
                    throw new APIException("No response returned. Does the player profile exist?");
                JSONObject jo = new JSONObject(Utils.convertStreamToString(responseEntity.getContent()));
                if (jo.has("error")) {
                    currentAttempts++;
                    errorMessage = jo.toString();
                    try {
                        Thread.sleep(RATE_LIMIT_TRY_AGAIN);
                        System.err.println("[MojangAPI] [getUUID] An error occurred! Are we being rate limited? Re-trying request in 1000 ms...");
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                    continue;
                }

                UUID uuid = UUIDUtil.createUUID(jo.getString("id")).get();
                // start an async process to add this to cache
                // without slowing down this current lookup
                if (!lookingUp.contains(username)) {
                    service.submit(() -> {
                        lookingUp.add(username);
                        try {
                            this.getProfile(uuid, ignoreCache);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        lookingUp.remove(username);
                    });
                }

                return uuid;
            }
        } while (currentAttempts < MAX_ATTEMPTS);

        throw new APIException(errorMessage);
    }

    public String getUsername(UUID uuid, boolean ignoreCache) throws IOException {
        return getProfile(uuid, ignoreCache).getUsername();
    }

    public MojangProfile getProfile(UUID uuid, boolean ignoreCache) throws IOException {
        assert uuid != null;

        if (!ignoreCache && PROFILE_CACHE.containsKey(uuid)) {
            return PROFILE_CACHE.get(uuid);
        }

        String usernameToUuid = "https://sessionserver.mojang.com/session/minecraft/profile/%s";
        usernameToUuid = String.format(usernameToUuid, uuid.toString().replace("-", ""));

        int currentAttempts = 0;
        String errorMessage;
        do {
            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                HttpGet get = new HttpGet(usernameToUuid);
                CloseableHttpResponse response = httpClient.execute(get);
                HttpEntity responseEntity = response.getEntity();
                if (responseEntity == null)
                    throw new APIException("No response returned. Does the player profile exist?");
                JSONObject profile = new JSONObject(Utils.convertStreamToString(responseEntity.getContent()));
                if (profile.has("error")) {
                    currentAttempts++;
                    errorMessage = profile.toString();
                    try {
                        Thread.sleep(RATE_LIMIT_TRY_AGAIN);
                        System.err.println("[MojangAPI] [getProfile] An error occurred! Are we being rate limited? Re-trying request in 1000 ms...");
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                    continue;
                }

                JSONObject textures = null;
                for (Object jo : profile.getJSONArray("properties")) {
                    JSONObject jsonObject = new JSONObject(jo.toString());
                    if (jsonObject.getString("name").equals("textures")) {
                        textures = new JSONObject(new String(Base64.getDecoder().decode(jsonObject.getString("value")))).getJSONObject("textures");
                        break;
                    }
                }

                assert textures != null;

                MojangProfile mp = new MojangProfile(
                        uuid,
                        profile.getString("name"),
                        MojangProfile.SkinModel.fromString(textures.getJSONObject("SKIN").has("metadata") ? textures.getJSONObject("SKIN").getJSONObject("metadata").getString("model") : null),
                        textures.getJSONObject("SKIN").getString("url"),
                        textures.has("CAPE") ? textures.getJSONObject("CAPE").getString("url") : null,
                        System.currentTimeMillis()
                );
                PROFILE_CACHE.put(uuid, mp);

                return mp;
            }
        } while (currentAttempts < MAX_ATTEMPTS);

        throw new APIException(errorMessage);
    }

}
