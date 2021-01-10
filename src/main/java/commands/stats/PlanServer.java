package commands.stats;

public enum PlanServer {

    GTM1("186b8901-2490-466a-bcc9-7af1acddbb63"),
    GTM4("b4dde0aa-e21e-4cf2-b1d3-94ec5949e5ab"),
    GTM6("069de149-3cf5-4872-b48a-97cc38779113"),
    GTM7("afd204ac-e026-41af-a52a-7824a12c3581"),
    HUB1("d21362d6-deee-4b93-a84a-7c596b1c9831"),
    HUB2("9eb1b597-541a-4c3f-bcfd-332fe27cacad"),
    HUB3("2bcc6fca-0ebd-4f48-b856-939c8456d7fb"),
    HUB4("6d8117cc-5654-4971-a8e6-36fd3295e3a6"),
    HUB5("88e70340-802f-4aaa-b1c9-9350393dc721"),
    CREATIVE1("1d652294-eaaf-4091-82bd-f0f212433dff"),
    STAFFSERVER1("18e70d47-d073-4bb1-9b97-356065c4bab7"),
    BUNGEECORD("9596a160-015a-4ffd-9df5-46ce6d2509e0"),
    UNKNOWN(null)
    ;

    private String uuid;

    PlanServer(String uuid) {
        this.uuid = uuid;
    }

    public String getUuid() {
        return uuid;
    }

    public static PlanServer getFromUUID (String uuid) {
        for (PlanServer ps : PlanServer.values()) {
            if (ps != PlanServer.UNKNOWN && ps.getUuid().equals(uuid))
                return ps;
        }
        return UNKNOWN;
    }

}
