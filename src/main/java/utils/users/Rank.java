package utils.users;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import utils.confighelpers.Config;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static utils.Utils.JDA;

public enum Rank {

    MANAGER ("MANAGER", Config.get().getRankSettings().getManager()),
    DEV ("DEV", Config.get().getRankSettings().getDeveloper()),
    ADMIN ("ADMIN", Config.get().getRankSettings().getAdmin()),
    BUILDER ("BUILDER", Config.get().getRankSettings().getBuilder()),
    SRMOD ("SRMOD", Config.get().getRankSettings().getSrMod()),
    MOD ("MOD", Config.get().getRankSettings().getMod()),
    HELPER ("HELPER", Config.get().getRankSettings().getHelper()),
    BUILDTEAM ("BUILDTEAM", Config.get().getRankSettings().getBuildTeam()),
    YOUTUBER ("YOUTUBE", Config.get().getRankSettings().getYoutuber()),
    SUPREME ("SUPREME", Config.get().getRankSettings().getSupreme()),
    SPONSOR ("SPONSOR", Config.get().getRankSettings().getSponsor()),
    ELITE ("ELITE", Config.get().getRankSettings().getElite()),
    PREMIUM ("PREMIUM", Config.get().getRankSettings().getPremium()),
    VIP ("VIP", Config.get().getRankSettings().getVip()),
    NORANK ("DEFAULT", Config.get().getRankSettings().getNoRank()),
    ;

    /** rank as known in the database */
    private String name;
    /** role id on discord */
    private long roleId;

    Rank(String name, long roleId) {
        this.name = name;
        this.roleId = roleId;
    }

    public boolean isHigherOrEqualTo(Rank role) {
        // works because enums are defined in rank order
        return this.getIndex() <= role.getIndex();
    }

    // lower index = higher role
    public int getIndex() {
        int index = 0;
        for (Rank r : Rank.values()) {
            if (this != r) index++;
            else return index;
        }
        return -1;
    }

    public static boolean hasRolePerms(Member member, @Nullable Rank role) {
        if (role == null) return true;

        // Check perms
        boolean roleMatch = false;
        List<Role> memberRole = member.getRoles();

        for (Role r : role.getRolesAbove()) {
            if (memberRole.contains(r)) {
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
        return JDA.getRoleById(roleId);
    }

    public static Rank getRankFromString(String rString) {
        if (rString.equalsIgnoreCase("HELPOP")) rString = "HELPER"; //some parts of gtm still refer helper as helpop

        String finalRString = rString;
        return Arrays.stream(Rank.values()).filter( (rank) -> rank.name.equalsIgnoreCase(finalRString)).findFirst().orElse(null);
    }

}
