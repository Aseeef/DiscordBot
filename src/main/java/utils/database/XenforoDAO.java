package utils.database;

import utils.database.sql.BaseDatabase;
import utils.tools.GTools;
import xenforo.objects.TicketMessage;
import xenforo.objects.XenforoUser;
import xenforo.objects.tickets.SupportTicket;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class XenforoDAO {

    public static TicketMessage getTicketMessage(int ticketId, int msgId) {
        try (Connection conn = BaseDatabase.getInstance(BaseDatabase.Database.XEN).getConnection()) {
            return getTicketMessage(conn, ticketId, msgId);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static TicketMessage getTicketMessage(Connection conn, int ticketId, int msgId) {

        String query = "SELECT * FROM `xf_brivium_support_ticket_message` WHERE `support_ticket_id`=? AND `message_id`=?;";

        try (PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setInt(1, ticketId);
            statement.setInt(2, msgId);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return new TicketMessage(
                            result.getInt("message_id"),
                            result.getInt("support_ticket_id"),
                            result.getInt("message_date"),
                            result.getInt("user_id"),
                            result.getString("username"),
                            result.getString("user_email"),
                            result.getString("message")
                    );
                }
            }
        } catch (SQLException e) {
            GTools.printStackError(e);
        }

        return null;
    }

    public static List<SupportTicket> getAllTicketsFrom (String username) {
        List<SupportTicket> ticketsList = new ArrayList<>();

        List<String> names = GTools.getAllUsernames(username);
        if (names == null || names.size() == 0) return new ArrayList<>();

        // extract unique usernames
        List<String> uniqueNames = new ArrayList<>();
        for (String name : names) {
            if (uniqueNames.stream().noneMatch(s -> s.equalsIgnoreCase(name))) {
                uniqueNames.add(name);
            }
        }

        try (Connection conn = BaseDatabase.getInstance(BaseDatabase.Database.XEN).getConnection()) {
            for (String u : uniqueNames) {
                ticketsList.addAll(XenforoDAO.searchTickets(conn, u));
            }
        } catch (SQLException e) {
            GTools.printStackError(e);
        }

        return ticketsList;
    }

    /** Get a list of all tickets from the specified username.
     *
     * @param conn - Connection
     * @param username - The username who's ticket to search
     * @return - List of tickets from the username
     */
    public static List<SupportTicket> searchTickets(Connection conn, String username) {

        String query = "SELECT * FROM `xf_brivium_support_ticket` WHERE LOWER(CONVERT(`custom_support_ticket_fields` USING latin1)) LIKE '%s:8:\"username\";s:" + username.length() + ":\"" + username + "\"%';";

        List<SupportTicket> tickets = new ArrayList<>();

        try (PreparedStatement statement = conn.prepareStatement(query)) {
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    tickets.add(new SupportTicket(
                            rs.getInt("support_ticket_id"),
                            rs.getString("ticket_id"),
                            rs.getString("title"),
                            rs.getInt("user_id"),
                            rs.getString("username"),
                            rs.getString("user_email"),
                            rs.getInt("openner_user_id"),
                            rs.getString("openner_username"),
                            rs.getInt("open_date"),
                            rs.getString("urgency"),
                            rs.getInt("reply_count"),
                            rs.getInt("participant_count"),
                            rs.getInt("department_id"),
                            rs.getInt("assigned_user_id"),
                            rs.getInt("ticket_status_id"),
                            rs.getInt("first_message_id"),
                            rs.getInt("last_update"),
                            rs.getInt("last_message_date"),
                            rs.getInt("last_message_id"),
                            rs.getFloat("submitter_rating"),
                            rs.getString("custom_support_ticket_fields"),
                            rs.getString("participants")
                    ));
                }
            }
        } catch (SQLException e) {
            GTools.printStackError(e);
        }

        // sort by open date
        tickets.sort(Comparator.comparingInt(SupportTicket::getOpenDate));

        return tickets;

    }

    public static List<SupportTicket> getPendingTickets() {
        List<SupportTicket> activeTickets = new ArrayList<>();

        try (Connection conn = BaseDatabase.getInstance(BaseDatabase.Database.XEN).getConnection()) {

            String query = "SELECT * FROM `xf_brivium_support_ticket` WHERE ticket_status_id != 4 AND ticket_status_id != 5 AND ticket_status_id != 2";

            try (PreparedStatement ps = conn.prepareStatement(query)) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        SupportTicket supportTicket = new SupportTicket(
                                rs.getInt("support_ticket_id"),
                                rs.getString("ticket_id"),
                                rs.getString("title"),
                                rs.getInt("user_id"),
                                rs.getString("username"),
                                rs.getString("user_email"),
                                rs.getInt("openner_user_id"),
                                rs.getString("openner_username"),
                                rs.getInt("open_date"),
                                rs.getString("urgency"),
                                rs.getInt("reply_count"),
                                rs.getInt("participant_count"),
                                rs.getInt("department_id"),
                                rs.getInt("assigned_user_id"),
                                rs.getInt("ticket_status_id"),
                                rs.getInt("first_message_id"),
                                rs.getInt("last_update"),
                                rs.getInt("last_message_date"),
                                rs.getInt("last_message_id"),
                                rs.getFloat("submitter_rating"),
                                rs.getString("custom_support_ticket_fields"),
                                rs.getString("participants")
                        );
                        activeTickets.add(supportTicket);
                    }
                }
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return activeTickets;
    }

    public static XenforoUser xenforoUserFromId (int id) {

        try (Connection conn = BaseDatabase.getInstance(BaseDatabase.Database.XEN).getConnection()) {

            String query = "SELECT * FROM `xf_user` WHERE user_id = ?";

            try (PreparedStatement ps = conn.prepareStatement(query)) {

                ps.setInt(1, id);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new XenforoUser(
                                id,
                                rs.getString("username"),
                                rs.getString("custom_title"),
                                rs.getString("email"),
                                rs.getString("timezone"),
                                rs.getString("gender"),
                                rs.getString("user_state"),
                                rs.getInt("register_date")
                        );
                    }
                }

            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return null;

    }

}
