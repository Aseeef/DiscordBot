package Utils;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

import java.util.ArrayList;
import java.util.List;

import static Utils.tools.GTools.jda;

public enum Rank {

    MANAGER ("MANAGER", Config.get().getManager()),
    DEV ("DEV", Config.get().getDeveloper()),
    ADMIN ("ADMIN", Config.get().getAdmin()),
    BUILDER ("BUILDER", Config.get().getBuilder()),
    SRMOD ("SRMOD", Config.get().getSrMod()),
    MOD ("MOD", Config.get().getMod()),
    HELPER ("HELPER", Config.get().getHelper()),
    BUILDTEAM ("BUILDTEAM", Config.get().getBuildTeam()),
    YOUTUBER ("YOUTUBE", Config.get().getYoutuber()),
    SUPREME ("SUPREME", Config.get().getSupreme()),
    SPONSOR ("SPONSOR", Config.get().getSponsor()),
    ELITE ("ELITE", Config.get().getElite()),
    PREMIUM ("PREMIUM", Config.get().getPremium()),
    VIP ("VIP", Config.get().getVip()),
    NORANK ("DEFAULT", Config.get().getNoRank()),
    NONE (null, -1),
    UNVERIFIED (null, Config.get().getUnverified()),
    ;

    private String name;
    private long roleId;

    Rank(String name, long roleId) {
        this.name = name;
        this.roleId = roleId;
    }


    public static boolean hasRolePerms(Member member, Rank role) {
        if (role == Rank.NONE) return true;

        // Check perms
        boolean roleMatch = false;
        List<Role> memberRoles = member.getRoles();

        for (Role r : role.getRolesAbove()) {
            if (memberRoles.contains(r)) {
                roleMatch = true;
                break;
            }
        }

        return roleMatch;
    }

    // Gets all roles above (useful for permission inheritance)
    public ArrayList<Role> getRolesAbove() {
        ArrayList<Role> roles = new ArrayList<>();
        if (this == Rank.MANAGER) {
            for (int i = 0 ; i < 1 ; i++) {
                roles.add(Rank.values()[i].getRole());
            }
        }
        else if (this == Rank.DEV) {
            for (int i = 0 ; i < 2 ; i++) {
                roles.add(Rank.values()[i].getRole());
            }
        }
        else if (this == Rank.ADMIN) {
            for (int i = 0 ; i < 3 ; i++) {
                roles.add(Rank.values()[i].getRole());
            }
        }
        else if (this == Rank.BUILDER) {
            for (int i = 0 ; i < 4 ; i++) {
                roles.add(Rank.values()[i].getRole());
            }
        }
        else if (this == Rank.SRMOD) {
            for (int i = 0 ; i < 5 ; i++) {
                roles.add(Rank.values()[i].getRole());
            }
        }
        else if (this == Rank.MOD) {
            for (int i = 0 ; i < 6 ; i++) {
                roles.add(Rank.values()[i].getRole());
            }
        }
        else if (this == Rank.HELPER) {
            for (int i = 0 ; i < 7 ; i++) {
                roles.add(Rank.values()[i].getRole());
            }
        }
        else if (this == Rank.BUILDTEAM) {
            for (int i = 0 ; i < 8 ; i++) {
                roles.add(Rank.values()[i].getRole());
            }
        }
        else if (this == Rank.YOUTUBER) {
            for (int i = 0 ; i < 9 ; i++) {
                roles.add(Rank.values()[i].getRole());
            }
        }
        else if (this == Rank.SUPREME) {
            for (int i = 0 ; i < 10 ; i++) {
                roles.add(Rank.values()[i].getRole());
            }
        }
        else if (this == Rank.SPONSOR) {
            for (int i = 0 ; i < 11 ; i++) {
                roles.add(Rank.values()[i].getRole());
            }
        }
        else if (this == Rank.ELITE) {
            for (int i = 0 ; i < 12 ; i++) {
                roles.add(Rank.values()[i].getRole());
            }
        }
        else if (this == Rank.PREMIUM) {
            for (int i = 0 ; i < 13 ; i++) {
                roles.add(Rank.values()[i].getRole());
            }
        }
        else if (this == Rank.VIP) {
            for (int i = 0 ; i < 14 ; i++) {
                roles.add(Rank.values()[i].getRole());
            }
        }
        else if (this == Rank.NORANK) {
            for (int i = 0 ; i < 15 ; i++) {
                roles.add(Rank.values()[i].getRole());
            }
        }
        // Any Non-GTM roles (like RANK.UNVERIFIED) will always ONLY return its self
        else roles.add(this.getRole());

        return roles;
    }

    /**
     * @return String rank name as on GTM
     */
    public String n() {
        return name;
    }

    public Role getRole() {
        return jda.getRoleById(roleId);
    }

    public static Rank getRankFromString(String rString) {
        for (Rank r : Rank.values()) {
            if (r.name.equals(rString.toUpperCase()))
                return r;
        }
        return null;
    }

}
