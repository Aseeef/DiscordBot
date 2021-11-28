package commands.stats.wrappers;

import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
public class GTMGang {

    private int gangId;
    private String server;
    private String name;
    private String description;
    private List<UUID> membersUUID;
    private List<String> members;

    public GTMGang(int gangId, String server, String name, String description, List<UUID> membersUUID, List<String> members) {
        this.gangId = gangId;
        this.server = server;
        this.name = name;
        this.description = description;
        this.membersUUID = membersUUID;
        this.members = members;
    }

}
