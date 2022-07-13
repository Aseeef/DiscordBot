package net.grandtheftmc.discordbot.commands.stats;

import net.grandtheftmc.discordbot.commands.stats.wrappers.*;
import net.grandtheftmc.discordbot.utils.database.sql.BaseDatabase;
import org.jetbrains.annotations.Nullable;
import net.grandtheftmc.discordbot.utils.UUIDUtil;

import java.sql.*;
import java.time.Instant;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class StatsDAO {

    public static UserProfile getUserProfile(UUID uuid) {
        try (Connection conn = BaseDatabase.getInstance(BaseDatabase.Database.USERS).getConnection()) {

            String query = "SELECT lastname,join_date,last_login FROM users WHERE uuid=?;";

            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setString(1, uuid.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new UserProfile(
                                uuid,
                                rs.getString("lastname"),
                                rs.getTimestamp("join_date"),
                                rs.getTimestamp("last_login")
                        );
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

        try (Connection conn = BaseDatabase.getInstance(BaseDatabase.Database.USERS).getConnection()) {
            String query = "SELECT session_start,session_end,afk_time,server_key FROM user_sessions WHERE uuid=UNHEX(?) AND session_start > ? AND session_start < ?";
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setString(1, uuid.toString().replace("-", ""));
                ps.setLong(2, from);
                ps.setLong(3, to);

                try (ResultSet rs = ps.executeQuery()) {

                    List<Session> sessionsList = new ArrayList<>();

                    while (rs.next()) {
                        long start = rs.getLong("session_start");
                        long end = rs.getLong("session_end");
                        long afkTime = rs.getLong("afk_time");
                        String server = rs.getString("server_key");
                        sessionsList.add(new Session(start, end, afkTime, Server.from(server.toUpperCase())));
                    }

                    return sessionsList;
                } catch (Exception ex) {
                    ex.printStackTrace();
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
    public static List<String> getAllAlts(UUID uuid, List<String> ipHistory) {
        List<String> alts = new ArrayList<>();

        for (String ip : ipHistory) {
            for (String newAlt : getAlts(uuid, ip))
                if (!alts.contains(newAlt))
                    alts.add(newAlt);
        }

        return alts;

    }

    public static List<WrappedPunishment> getPunishments (UUID uuid, WrappedPunishment.PunishmentType punishmentType, boolean getBanner) {

        List<WrappedPunishment> punishments = new ArrayList<>();
        Pattern uuidPattern = Pattern.compile("\\b[0-9a-f]{8}\\b-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-\\b[0-9a-f]{12}\\b");

        try (Connection conn = BaseDatabase.getInstance(BaseDatabase.Database.BANS).getConnection()) {
            String query = "SELECT * FROM `" + punishmentType.getTable() + "` WHERE " + (getBanner ? "banned_by_uuid" : "uuid") + "=?;";
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setString(1, uuid.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int id = rs.getInt("id");
                        String ip = rs.getString("ip");
                        String reason = rs.getString("reason");
                        String banningName = rs.getString("banned_by_name");
                        String banningUUIDString = rs.getString("banned_by_uuid");
                        UUID banningUUID = banningUUIDString == null || !uuidPattern.matcher(banningUUIDString).find() ? null : UUID.fromString(banningUUIDString);
                        String unbanName = punishmentType == WrappedPunishment.PunishmentType.KICK ? null : rs.getString("removed_by_name");
                        String unbanUUIDString = punishmentType == WrappedPunishment.PunishmentType.KICK ? null : rs.getString("removed_by_uuid");
                        UUID unbanUUID = unbanUUIDString == null || !uuidPattern.matcher(unbanUUIDString).find() ? null : UUID.fromString(unbanUUIDString);
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

    public static List<GTMGang> getGangs (UUID uuid) {

        List<GTMGang> gangs = new ArrayList<>();

        try (Connection conn = BaseDatabase.getInstance(BaseDatabase.Database.USERS).getConnection()) {

            List<Integer> gangIds = new ArrayList<>();

            String query1 = "SELECT `gang_id` FROM `gtm_gang_member` WHERE HEX(uuid)=?";
            try (PreparedStatement ps = conn.prepareStatement(query1)) {
                ps.setString(1, uuid.toString().replace("-", ""));
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        gangIds.add(rs.getInt("gang_id"));
                    }
                }
            }

            for (int gangId : gangIds) {
                gangs.add(getGang(gangId));
            }

            return gangs;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;

    }

    public static GTMGang getGang (int gangId) {

        String server = null;
        String name = null;
        String description = null;
        List<UUID> members = new ArrayList<>();
        List<String> memberNames = new ArrayList<>();

        try (Connection conn = BaseDatabase.getInstance(BaseDatabase.Database.USERS).getConnection()) {

            String query2 = "SELECT * FROM `gtm_gang` WHERE id=?;";
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

            String query3 = "SELECT HEX(uuid) FROM `gtm_gang_member` WHERE gang_id=?;";
            try (PreparedStatement ps = conn.prepareStatement(query3)) {
                ps.setInt(1, gangId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        UUID memberUUID = UUIDUtil.createUUID(rs.getString("HEX(uuid)")).orElse(null);
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

    private static final String questionTable = "helpquestions";
    private static final String answerTable = "helpanswers";

    /**
     * @return - HelpQuestions where one of the answerer was the specified staff uuid.
     * If staff UUID is null, will return help questions for Larry.
     */
    public static LinkedList<HelpQuestion> getHelpQuestions (long sinceAfter, @Nullable UUID staff) {
        // create list
        List<Integer> questionIds = new ArrayList<>();
        // construct search parameter
        String search = staff == null ? null : staff.toString().replace("-", "");

        String query1 = "SELECT question_id FROM " + answerTable + " WHERE reply_time > ? AND HEX(staff_uuid) = ?;";
        String query2 = "SELECT question_id FROM " + answerTable + " WHERE reply_time > ? AND staff_uuid IS NULL;";

        try (Connection connection = BaseDatabase.getInstance(BaseDatabase.Database.USERS).getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(search == null ? query2 : query1)) {
                // set time
                statement.setTimestamp(1, new Timestamp(sinceAfter));
                // set staff if it isnt null
                if (search != null) {
                    statement.setString(2, search);
                }

                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        questionIds.add(rs.getInt("question_id"));
                    }
                }

            }

            return getHelpQuestions(connection, questionIds);

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new LinkedList<>();
    }

    public static LinkedList<HelpQuestion> getHelpQuestions (Connection connection, List<Integer> questionIds) {

        LinkedList<HelpQuestion> questions = new LinkedList<>();

        int upperLimit = questionIds.stream().mapToInt(i -> i).max().orElse(-1);
        int lowerLimit = questionIds.stream().mapToInt(i -> i).min().orElse(-1);
        if (upperLimit < 0 || lowerLimit < 0) return questions;

        String query = "SELECT question_id, HEX(asker_uuid), question, server_key, ask_time, online_staff, close_reason FROM " + questionTable + " WHERE question_id > ? AND question_id < ? ORDER BY ask_time DESC;";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            // set lower and upper limit for questions
            statement.setInt(1, lowerLimit);
            statement.setInt(2, upperLimit);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    // if this isn't a question id we are interested in continue
                    if (!questionIds.contains(rs.getInt("question_id"))) continue;

                    HelpQuestion helpQuestion = constructHelpQuestion(rs);
                    questions.add(helpQuestion);
                }
            }

            // population questions with answers
            populationAnswers(connection, questions);

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return questions;

    }

    public static void populationAnswers (Connection connection, LinkedList<HelpQuestion> questions) {

        int upperLimit = questions.stream().mapToInt(HelpQuestion::getQuestionId).max().orElse(-1);
        int lowerLimit = questions.stream().mapToInt(HelpQuestion::getQuestionId).min().orElse(-1);
        if (upperLimit < 0 || lowerLimit < 0) return;

        String query2 = "SELECT question_id, HEX(staff_uuid), answer, reply_time FROM " + answerTable + " WHERE question_id > ? AND question_id < ? ORDER BY reply_time ASC;";

        try (PreparedStatement statement = connection.prepareStatement(query2)) {
            // set lower and upper limit for question ids in answers
            statement.setInt(1, lowerLimit);
            statement.setInt(2, upperLimit);

            // populate question with answers in order of first answered to last
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    int questionId = rs.getInt("question_id");
                    Optional<HelpQuestion> optQuestion = questions.stream().filter(q -> q.getQuestionId() == questionId).findFirst();
                    if (optQuestion.isPresent()) {
                        optQuestion.get().getAnswers().add(constructHelpAnswer(rs));
                    }
                }
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private static HelpQuestion constructHelpQuestion(ResultSet rs) throws SQLException {

        String rawStaffList = rs.getString("online_staff");
        List<String> stringUUIDs = rawStaffList == null ? new ArrayList<>() : Arrays.asList(rawStaffList.split(","));
        List<UUID> staffUUIDs = stringUUIDs.stream().map((sUUID) -> UUIDUtil.createUUID(sUUID).orElse(null)).collect(Collectors.toList());

        return new HelpQuestion(
                rs.getInt("question_id"),
                UUIDUtil.createUUID(rs.getString("HEX(asker_uuid)")).orElse(null),
                rs.getString("question"), rs.getString("server_key"),
                rs.getTimestamp("ask_time").getTime(),
                staffUUIDs,
                new LinkedList<>(),
                HelpQuestion.CloseReason.getReason(rs.getInt("close_reason"))
        );
    }

    private static HelpAnswer constructHelpAnswer(ResultSet rs) throws SQLException {
        return new HelpAnswer(
                // staff_uuid creation will result in null when the answerer is larry
                UUIDUtil.createUUID(rs.getString("HEX(staff_uuid)")).orElse(null),
                rs.getString("answer"),
                rs.getTimestamp("reply_time").getTime()
        );
    }

}
