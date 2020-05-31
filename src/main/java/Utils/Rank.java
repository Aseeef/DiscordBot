package Utils;

import net.dv8tion.jda.api.entities.Role;

import java.util.ArrayList;

import static Utils.tools.GTools.jda;

public enum Rank {

    MANAGER ("MANAGER", jda.getRoleById(Config.get().getManager())),
    DEV ("DEV", jda.getRoleById(Config.get().getDeveloper())),
    ADMIN ("ADMIN", jda.getRoleById(Config.get().getAdmin())),
    BUILDER ("BUILDER", jda.getRoleById(Config.get().getBuilder())),
    SRMOD ("SRMOD", jda.getRoleById(Config.get().getSrMod())),
    MOD ("MOD", jda.getRoleById(Config.get().getMod())),
    HELPER ("HELPER", jda.getRoleById(Config.get().getHelper())),
    BUILDTEAM ("BUILDTEAM", jda.getRoleById(Config.get().getBuildTeam())),
    YOUTUBER ("YOUTUBE", jda.getRoleById(Config.get().getYoutuber())),
    SUPREME ("SUPREME", jda.getRoleById(Config.get().getSupreme())),
    SPONSOR ("SPONSOR", jda.getRoleById(Config.get().getSponsor())),
    ELITE ("ELITE", jda.getRoleById(Config.get().getElite())),
    PREMIUM ("PREMIUM", jda.getRoleById(Config.get().getPremium())),
    VIP ("VIP", jda.getRoleById(Config.get().getVip())),
    NORANK ("DEFAULT", jda.getRoleById(Config.get().getNoRank())),
    UNVERIFIED (null, jda.getRoleById(Config.get().getUnverified())),
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

    public static Rank getRankFromString(String rString) {
        for (Rank r : Rank.values()) {
            if (r.name.equals(rString.toUpperCase()))
                return r;
        }
        return null;
    }

}
