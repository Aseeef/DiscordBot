package net.grandtheftmc.discordbot.commands.stats;

import lombok.Getter;
import net.grandtheftmc.ServerType;

@Getter
public enum Server {

    GTM1(ServerType.GTM),
    GTM4(ServerType.GTM),
    GTM6(ServerType.GTM),
    GTM7(ServerType.GTM),
    GTM8(ServerType.GTM),
    HUB1(ServerType.HUB),
    HUB2(ServerType.HUB),
    HUB3(ServerType.HUB),
    HUB4(ServerType.HUB),
    CREATIVE1(ServerType.CREATIVE),
    BUNGEE1(ServerType.PROXY),
    UNKNOWN(null)
    ;

    private final ServerType serverType;

    Server(ServerType serverType) {
        this.serverType = serverType;
    }

    public static Server from(String s) {
        for (Server st : Server.values()) {
            if (st.toString().equalsIgnoreCase(s)) {
                return st;
            }
        }
        return Server.UNKNOWN;
    }

}
