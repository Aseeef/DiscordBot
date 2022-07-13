package net.grandtheftmc.discordbot.utils.database.redis;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter @AllArgsConstructor
public class DupeAlert {

    private String name;
    private UUID duperUUID;
    private long detectionTime;
    private String detectionType;

}
