package commands.stats;

import com.mysql.jdbc.jdbc2.optional.SuspendableXAConnection;
import commands.stats.wrappers.*;
import org.jetbrains.annotations.Nullable;
import utils.database.sql.BaseDatabase;
import utils.tools.UUIDUtil;

import java.sql.*;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

public class StatsDAO {

    public static PlanUser getPlanUser (UUID uuid) {

        try (Connection conn = BaseDatabase.getInstance(BaseDatabase.Database.PLAN).getConnection()) {
            String query = "SELECT * FROM `plan_users` WHERE uuid=?";
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setString(1, uuid.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String name = rs.getString("name");
                        long registered = rs.getLong("registered");
                        int timesKicked = rs.getInt("times_kicked");
                        return new PlanUser(uuid, name, registered, timesKicked);
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public static List<Session> getSessions (UUID uuid, @Nullable Long from, @Nullable Long to) {

        if (from == null)
            from = 0L;
        if (to == null)
            to = System.currentTimeMillis();

        try (Connection conn = BaseDatabase.getInstance(BaseDatabase.Database.PLAN).getConnection()) {
            String query = "SELECT * FROM `plan_sessions` WHERE uuid=? AND session_start > ? AND session_start < ?";
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setString(1, uuid.toString());
                ps.setLong(2, from);
                ps.setLong(3, to);
                try (ResultSet rs = ps.executeQuery()) {

                    List<Session> sessionsList = new ArrayList<>();

                    while (rs.next()) {
                        long playtime = rs.getLong("session_end") - rs.getLong("session_start");
                        long afkTime = rs.getLong("afk_time");
                        PlanServer server = PlanServer.getFromUUID(rs.getString("server_uuid"));
                        sessionsList.add(new Session(playtime, afkTime, server));
                    }

                    return sessionsList;
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return new ArrayList<>();
    }

    public static WrappedIPData getIpInfo(String ip) {

        try (Connection conn = BaseDatabase.getInstance(BaseDatabase.Database.USERS).getConnection()) {

        try (PreparedStatement statement = conn.prepareStatement("SELECT * FROM `ip_info` WHERE ip=INET_ATON(?);")) {

            statement.setString(1, ip);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {

                    Double latitude = rs.getDouble("latitude");
                    boolean null1 = rs.wasNull();
                    Double longitude = rs.getDouble("longitude");
                    boolean null2 = rs.wasNull();
                    if (null1) {
                        latitude = null;
                    }
                    if (null2) {
                        longitude = null;
                    }

                    return new WrappedIPData(
                            ip,
                            rs.getString("asn"),
                            rs.getString("provider"),
                            rs.getString("country"),
                            rs.getString("isocode"),
                            rs.getString("city"),
                            rs.getString("region"),
                            rs.getString("regioncode"),
                            latitude,
                            longitude,
                            TimeZone.getTimeZone(rs.getString("timezone")),
                            rs.getBoolean("proxy"),
                            rs.getString("ip_type"),
                            rs.getByte("risk"),
                            rs.getTimestamp("updated")
                    );

                }
            }
        }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Note: The IPs at the lowest index are the most recent ips
     */
    public static List<String> getIPs (UUID uuid) {

        List<String> ips = new ArrayList<>();

        try (Connection conn = BaseDatabase.getInstance(BaseDatabase.Database.BANS).getConnection()) {
            String query = "SELECT * FROM `litebans_history` WHERE uuid=? ORDER BY date DESC";
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setString(1, uuid.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        ips.add(rs.getString("ip"));
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return ips;
    }

    /**
     * @return - Only the alts that are currently linked to this player through their most recent ip
     */
    public static List<String> getAlts(UUID uuid, String ip) {
        List<String> alts = new ArrayList<>();

        try (Connection conn = BaseDatabase.getInstance(BaseDatabase.Database.BANS).getConnection()) {
            String query = "SELECT * FROM `litebans_history` WHERE ip=? AND uuid NOT LIKE ?";
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setString(1, ip);
                ps.setString(2, uuid.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                       alts.add(rs.getString("name"));
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return alts;
    }

    /**
     * @return - The account name of every single alt that this player has ever been linked to through their entire ip history
     */
    public static List<String> getAllAlts(UUID uuid) {
        List<String> alts = new ArrayList<>();

        List<String> ips = getIPs(uuid);

        for (String ip : ips) {
            for (String newAlt : getAlts(uuid, ip))
                if (!alts.contains(newAlt))
                    alts.add(newAlt);
        }

        return alts;

    }

    public static List<WrappedPunishment> getPunishments (UUID uuid, WrappedPunishment.PunishmentType punishmentType) {

        List<WrappedPunishment> punishments = new ArrayList<>();

        try (Connection conn = BaseDatabase.getInstance(BaseDatabase.Database.BANS).getConnection()) {
            String query = "SELECT * FROM `" + punishmentType.getTable() + "` WHERE uuid=?";
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setString(1, uuid.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int id = rs.getInt("id");
                        String ip = rs.getString("ip");
                        String reason = rs.getString("reason");
                        String banningName = rs.getString("banned_by_name");
                        String banningUUIDString = rs.getString("banned_by_uuid");
                        UUID banningUUID = banningUUIDString == null ? null : UUID.fromString(banningUUIDString);
                        String unbanName = rs.getString("removed_by_name");
                        String unbanUUIDString = rs.getString("removed_by_uuid");
                        UUID unbanUUID = unbanUUIDString == null ? null : UUID.fromString(unbanUUIDString);
                        Timestamp time = Timestamp.from(Instant.ofEpochMilli(rs.getLong("time")));
                        long unbanOnRaw = rs.getLong("until");
                        Timestamp unbanOn = unbanOnRaw == -1 ? null : Timestamp.from(Instant.ofEpochMilli(unbanOnRaw));
                        String scope = rs.getString("server_scope");
                        String origin = rs.getString("server_origin");
                        boolean silent = rs.getBoolean("silent");
                        boolean ipBan = rs.getBoolean("ipban");
                        boolean active = rs.getBoolean("active");

                        punishments.add(new WrappedPunishment(id, uuid, punishmentType, ip, reason, banningUUID, banningName, unbanName, unbanUUID, time, unbanOn, scope, origin, silent, ipBan, active));
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return punishments;

    }

    public GTMGang getGang (UUID uuid) {

        try (Connection conn = BaseDatabase.getInstance(BaseDatabase.Database.USERS).getConnection()) {

            int gangId = -1;
            String server = null;
            String name = null;
            String description = null;
            List<UUID> members = new ArrayList<>();
            List<String> memberNames = new ArrayList<>();

            String query1 = "SELECT `gang_id` FROM `gtm_gang_member` WHERE HEX(uuid)=?";
            try (PreparedStatement ps = conn.prepareStatement(query1)) {
                ps.setString(1, uuid.toString().replace("-", ""));
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        gangId = rs.getInt("gang_id");
                    }
                }
            }

            if (gangId == -1) return null;

            String query2 = "SELECT * FROM `gtm_gang` WHERE id=?";
            try (PreparedStatement ps = conn.prepareStatement(query2)) {
                ps.setInt(1, gangId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        server = rs.getString("server_key");
                        name = rs.getString("name");
                        description = rs.getString("description");
                    }
                }
            }

            String query3 = "SELECT * FROM `gtm_gang_member` WHERE id=?";
            try (PreparedStatement ps = conn.prepareStatement(query3)) {
                ps.setInt(1, gangId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        UUID memberUUID = UUIDUtil.createUUID(rs.getString("uuid")).orElse(null);
                        if (memberUUID != null) members.add(memberUUID);
                    }
                }
            }

            String query4 = "SELECT * FROM `user` WHERE HEX(`uuid`)=?";
            for (UUID member : members) {
                try (PreparedStatement ps = conn.prepareStatement(query4)) {
                    ps.setString(1, member.toString().replace("-",""));
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            memberNames.add(rs.getString("name"));
                        }
                    }
                }
            }

            return new GTMGang(gangId, server, name, description, members, memberNames);

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;

    }

}
