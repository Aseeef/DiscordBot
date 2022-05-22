package commands.stats;

import lombok.Getter;

@Getter
public enum PlanServer {

    GTM1(3, "186b8901-2490-466a-bcc9-7af1acddbb63"),
    GTM4(8, "b4dde0aa-e21e-4cf2-b1d3-94ec5949e5ab"),
    GTM6(1, "069de149-3cf5-4872-b48a-97cc38779113"),
    GTM7(12, "afd204ac-e026-41af-a52a-7824a12c3581"),
    GTM8(71, "8558c48a-9bf9-4afe-b636-bcf5956ad212"),
    HUB1(5, "d21362d6-deee-4b93-a84a-7c596b1c9831"),
    HUB2(6, "9eb1b597-541a-4c3f-bcfd-332fe27cacad"),
    HUB3(9, "2bcc6fca-0ebd-4f48-b856-939c8456d7fb"),
    CREATIVE1(7, "1d652294-eaaf-4091-82bd-f0f212433dff"),
    BUNGEE1(2, "9596a160-015a-4ffd-9df5-46ce6d2509e0"),
    UNKNOWN(-1, null)
    ;

    private int id;
    private String uuid;

    PlanServer(int id, String uuid) {
        this.id = id;
        this.uuid = uuid;
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
