package net.grandtheftmc.discordbot.utils.confighelpers;

public class RankSettings {

    private long manager;
    private long developer;
    private long admin;
    private long builder;
    private long srMod;
    private long mod;
    private long helper;
    private long buildTeam;
    private long youtuber;
    private long supreme;
    private long sponsor;
    private long elite;
    private long premium;
    private long vip;
    private long noRank;

    public RankSettings() {
    }

    public RankSettings(long manager, long developer, long admin, long builder, long srMod, long mod, long helper, long buildTeam, long youtuber, long supreme, long sponsor, long elite, long premium, long vip, long noRank) {
        this.manager = manager;
        this.developer = developer;
        this.admin = admin;
        this.builder = builder;
        this.srMod = srMod;
        this.mod = mod;
        this.helper = helper;
        this.buildTeam = buildTeam;
        this.youtuber = youtuber;
        this.supreme = supreme;
        this.sponsor = sponsor;
        this.elite = elite;
        this.premium = premium;
        this.vip = vip;
        this.noRank = noRank;
    }

    public long getManager() {
        return manager;
    }

    public long getDeveloper() {
        return developer;
    }

    public long getAdmin() {
        return admin;
    }

    public long getBuilder() {
        return builder;
    }

    public long getSrMod() {
        return srMod;
    }

    public long getMod() {
        return mod;
    }

    public long getHelper() {
        return helper;
    }

    public long getBuildTeam() {
        return buildTeam;
    }

    public long getYoutuber() {
        return youtuber;
    }

    public long getSupreme() {
        return supreme;
    }

    public long getSponsor() {
        return sponsor;
    }

    public long getElite() {
        return elite;
    }

    public long getPremium() {
        return premium;
    }

    public long getVip() {
        return vip;
    }

    public long getNoRank() {
        return noRank;
    }
}
