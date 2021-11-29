package commands.stats.wrappers;

import lombok.Getter;

import java.util.UUID;

@Getter
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
}
