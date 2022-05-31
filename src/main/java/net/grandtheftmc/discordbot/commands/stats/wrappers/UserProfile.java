package net.grandtheftmc.discordbot.commands.stats.wrappers;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.sql.Timestamp;
import java.util.UUID;

@AllArgsConstructor
@Getter
public class UserProfile {

    UUID uuid;
    String username;
    Timestamp firstJoin;
    Timestamp lastJoin;

}
