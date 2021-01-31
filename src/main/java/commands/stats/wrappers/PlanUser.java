package commands.stats.wrappers;

import java.util.UUID;

public class PlanUser {

    private UUID uuid;
    private String username;
    private long registered;
    private int timesKicked;

    public PlanUser(UUID uuid, String username, long registered, int timesKicked) {
        this.uuid = uuid;
        this.username = username;
        this.registered = registered;
        this.timesKicked = timesKicked;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getUsername() {
        return username;
    }

    public long getRegistered() {
        return registered;
    }

    public int getTimesKicked() {
        return timesKicked;
    }
}
