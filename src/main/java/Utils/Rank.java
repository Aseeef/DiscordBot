package Utils;

import net.dv8tion.jda.api.entities.Role;

import java.util.ArrayList;

import static Utils.tools.GTools.jda;

public enum Rank {

    MANAGER ("MANAGER", jda.getRolesByName(Config.get().getManager(), true).get(0)),
    DEV ("DEV", jda.getRolesByName(Config.get().getDeveloper(), true).get(0)),
    ADMIN ("ADMIN", jda.getRolesByName(Config.get().getAdmin(), true).get(0)),
    BUILDER ("BUILDER", jda.getRolesByName(Config.get().getBuilder(), true).get(0)),
    SRMOD ("SRMOD", jda.getRolesByName(Config.get().getSrMod(), true).get(0)),
    MOD ("MOD", jda.getRolesByName(Config.get().getMod(), true).get(0)),
    HELPER ("HELPER", jda.getRolesByName(Config.get().getHelper(), true).get(0)),
    BUILDTEAM ("BUILDTEAM", jda.getRolesByName(Config.get().getBuildTeam(), true).get(0)),
    YOUTUBER ("YOUTUBE", jda.getRolesByName(Config.get().getYoutuber(), true).get(0)),
    SUPREME ("SUPREME", jda.getRolesByName(Config.get().getSupreme(), true).get(0)),
    SPONSOR ("SPONSOR", jda.getRolesByName(Config.get().getSponsor(), true).get(0)),
    ELITE ("ELITE", jda.getRolesByName(Config.get().getElite(), true).get(0)),
    PREMIUM ("PREMIUM", jda.getRolesByName(Config.get().getPremium(), true).get(0)),
    VIP ("VIP", jda.getRolesByName(Config.get().getVip(), true).get(0)),
    NORANK ("DEFAULT", jda.getRolesByName(Config.get().getNoRank(), true).get(0)),
    UNVERIFIED (null, jda.getRolesByName(Config.get().getUnverified(), true).get(0))
    ;

    private String name;
    private Role r;

    Rank(String name, Role r) {
        this.name = name;
        this.r = r;
    }

    // Gets all roles above (useful for permission inheritance)
    public ArrayList<Role> r() {
        ArrayList<Role> roles = new ArrayList<>();
        if (this == Rank.MANAGER) {
            for (int i = 0 ; i < 1 ; i++) {
                roles.add(Rank.values()[i].er());
            }
        }
        else if (this == Rank.DEV) {
            for (int i = 0 ; i < 2 ; i++) {
                roles.add(Rank.values()[i].er());
            }
        }
        else if (this == Rank.ADMIN) {
            for (int i = 0 ; i < 3 ; i++) {
                roles.add(Rank.values()[i].er());
            }
        }
        else if (this == Rank.BUILDER) {
            for (int i = 0 ; i < 4 ; i++) {
                roles.add(Rank.values()[i].er());
            }
        }
        else if (this == Rank.SRMOD) {
            for (int i = 0 ; i < 5 ; i++) {
                roles.add(Rank.values()[i].er());
            }
        }
        else if (this == Rank.MOD) {
            for (int i = 0 ; i < 6 ; i++) {
                roles.add(Rank.values()[i].er());
            }
        }
        else if (this == Rank.HELPER) {
            for (int i = 0 ; i < 7 ; i++) {
                roles.add(Rank.values()[i].er());
            }
        }
        else if (this == Rank.BUILDTEAM) {
            for (int i = 0 ; i < 8 ; i++) {
                roles.add(Rank.values()[i].er());
            }
        }
        else if (this == Rank.YOUTUBER) {
            for (int i = 0 ; i < 9 ; i++) {
                roles.add(Rank.values()[i].er());
            }
        }
        else if (this == Rank.SUPREME) {
            for (int i = 0 ; i < 10 ; i++) {
                roles.add(Rank.values()[i].er());
            }
        }
        else if (this == Rank.SPONSOR) {
            for (int i = 0 ; i < 11 ; i++) {
                roles.add(Rank.values()[i].er());
            }
        }
        else if (this == Rank.ELITE) {
            for (int i = 0 ; i < 12 ; i++) {
                roles.add(Rank.values()[i].er());
            }
        }
        else if (this == Rank.PREMIUM) {
            for (int i = 0 ; i < 13 ; i++) {
                roles.add(Rank.values()[i].er());
            }
        }
        else if (this == Rank.VIP) {
            for (int i = 0 ; i < 14 ; i++) {
                roles.add(Rank.values()[i].er());
            }
        }
        else if (this == Rank.NORANK) {
            for (int i = 0 ; i < 15 ; i++) {
                roles.add(Rank.values()[i].er());
            }
        }
        // Any Non-GTM roles (like RANK.UNVERIFIED) will always ONLY return its self
        else roles.add(this.er());

        return roles;
    }

    /**
     * @return String rank name as on GTM
     */
    public String n() {
        return name;
    }

    public Role er() {
        return r;
    }

}
