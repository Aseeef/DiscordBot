package net.grandtheftmc.discordbot.commands.stats.wrappers;

import lombok.Getter;

import java.util.UUID;

@Getter
public class PlanUser {

    private final UUID uuid;
    private final String username;
    private final long registered;
    private final int timesKicked;

    public PlanUser(UUID uuid, String username, long registered, int timesKicked) {
        this.uuid = uuid;
        this.username = username;
        this.registered = registered;
        this.timesKicked = timesKicked;
    }
}
