package net.grandtheftmc.discordbot.commands.stats.wrappers;

import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
public class GTMGang {

    private final int gangId;
    private final String server;
    private final String name;
    private final String description;
    private final List<UUID> membersUUID;
    private final List<String> members;

    public GTMGang(int gangId, String server, String name, String description, List<UUID> membersUUID, List<String> members) {
        this.gangId = gangId;
        this.server = server;
        this.name = name;
        this.description = description;
        this.membersUUID = membersUUID;
        this.members = members;
    }

}
