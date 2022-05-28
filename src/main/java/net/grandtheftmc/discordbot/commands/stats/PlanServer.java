package net.grandtheftmc.discordbot.commands.stats;

import lombok.Getter;
import net.grandtheftmc.ServerType;

@Getter
public enum PlanServer {

    GTM1(3, "186b8901-2490-466a-bcc9-7af1acddbb63", ServerType.GTM),
    GTM4(8, "b4dde0aa-e21e-4cf2-b1d3-94ec5949e5ab", ServerType.GTM),
    GTM6(1, "069de149-3cf5-4872-b48a-97cc38779113", ServerType.GTM),
    GTM7(12, "afd204ac-e026-41af-a52a-7824a12c3581", ServerType.GTM),
    GTM8(71, "8558c48a-9bf9-4afe-b636-bcf5956ad212", ServerType.GTM),
    HUB1(5, "d21362d6-deee-4b93-a84a-7c596b1c9831", ServerType.HUB),
    HUB2(6, "9eb1b597-541a-4c3f-bcfd-332fe27cacad", ServerType.HUB),
    HUB3(9, "2bcc6fca-0ebd-4f48-b856-939c8456d7fb", ServerType.HUB),
    CREATIVE1(7, "1d652294-eaaf-4091-82bd-f0f212433dff", ServerType.CREATIVE),
    BUNGEE1(2, "9596a160-015a-4ffd-9df5-46ce6d2509e0", ServerType.PROXY),
    UNKNOWN(-1, null, null)
    ;

    private final int id;
    private final String uuid;
    private final ServerType serverType;

    PlanServer(int id, String uuid, ServerType serverType) {
        this.id = id;
        this.uuid = uuid;
        this.serverType = serverType;
    }

    public static PlanServer getFromUUID (String uuid) {
        for (PlanServer ps : PlanServer.values()) {
            if (ps != PlanServer.UNKNOWN && ps.getUuid().equals(uuid))
                return ps;
        }
        return UNKNOWN;
    }

    public static PlanServer getFromID (int id) {
        for (PlanServer ps : PlanServer.values()) {
            if (ps != PlanServer.UNKNOWN && ps.getId() == id)
                return ps;
        }
        return UNKNOWN;
    }

}
