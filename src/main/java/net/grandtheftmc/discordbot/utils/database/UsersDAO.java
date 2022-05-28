package net.grandtheftmc.discordbot.utils.database;

import net.grandtheftmc.discordbot.utils.Utils;
import net.grandtheftmc.discordbot.utils.UUIDUtil;
import net.grandtheftmc.discordbot.utils.users.GTMUser;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UsersDAO {

    public static Object[] getGangMembersFor (Connection conn, GTMUser user, String server) {

        String gangName = null;
        List<GTMUser> gangMembers = new ArrayList<>();

        // Step 1: Gang all gangs player is in
        try (PreparedStatement ps = conn.prepareStatement("SELECT `gang_id` FROM `gtm_gang_member` WHERE `uuid`=UNHEX(?)")) {
            ps.setString(1, user.getUuid().toString().replaceAll("-", ""));
            List<Integer> gangIds = new ArrayList<>();
            try (ResultSet result = ps.executeQuery()) {
                while (result.next()) {
                    gangIds.add(result.getInt("gang_id"));
                }
            }

            // Step 2: Find which gang is it for given server
            Integer gangId = null;
            for (int i : gangIds) {
                try (PreparedStatement ps2 = conn.prepareStatement("SELECT * FROM `gtm_gang` WHERE `id`=" + i)) {
                    try (ResultSet result = ps2.executeQuery()) {
                        if (result.next()) {
                            if (result.getString("server_key").equalsIgnoreCase(server)) {
                                gangId = i;
                                gangName = result.getString("name");
                                break;
                            }
                        }
                    }
                }
            }

            // Step 3: Get all users with that gang id
            if (gangId != null) {
                List<UUID> gangUuids = new ArrayList<>();
                try (PreparedStatement ps3 = conn.prepareStatement("SELECT HEX(`uuid`) AS uuid FROM `gtm_gang_member` WHERE `gang_id`=" + gangId)) {
                    try (ResultSet result = ps3.executeQuery()) {
                        while (result.next()) {
                            UUIDUtil.createUUID(result.getString("uuid")).ifPresent(gangUuids::add);
                        }
                    }
                }

                GTMUser.getLoadedUsers().forEach(gtmUser -> {
                    if (gangUuids.contains(gtmUser.getUuid()))
                        gangMembers.add(gtmUser);
                });
            }

        } catch (Exception e) {
            Utils.printStackError(e);
        }

        return new Object[] {gangName, gangMembers};
    }

}
