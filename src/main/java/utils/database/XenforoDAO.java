package utils.database;

import utils.tools.GTools;
import xenforo.objects.tickets.SupportTicket;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class XenforoDAO {

    public static String getTicketMessage(Connection conn, int ticketId, int msgId) {

        String query = "SELECT * FROM `xf_brivium_support_ticket_message` WHERE `support_ticket_id`=? AND `message_id`=?;";

        try (PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setInt(1, ticketId);
            statement.setInt(2, msgId);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return result.getString("message");
                }
            }
        } catch (SQLException e) {
            GTools.printStackError(e);
        }

        return null;
    }

    /** Get a list of all tickets from the specified username.
     *
     * @param conn - Connection
     * @param username - The username who's ticket to search
     * @return - List of tickets from the username
     */
    public static List<SupportTicket> getAllTicketsFrom(Connection conn, String username) {

        String query = "SELECT * FROM `xf_brivium_support_ticket` WHERE LOWER(CONVERT(`custom_support_ticket_fields` USING latin1)) LIKE '%s:8:\"username\";s:" + username.length() + ":\"" + username + "\"%';";

        List<SupportTicket> tickets = new ArrayList<>();

        try (PreparedStatement statement = conn.prepareStatement(query)) {
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    tickets.add(new SupportTicket(
                            result.getInt(1),
                            result.getString(2),
                            result.getString(3),
                            result.getInt(4),
                            result.getString(5),
                            result.getString(6),
                            result.getInt(8),
                            result.getString(9),
                            result.getInt(10),
                            result.getString(11),
                            result.getInt(13),
                            result.getInt(14),
                            result.getInt(15),
                            result.getInt(16),
                            result.getInt(17),
                            result.getInt(19),
                            result.getInt(20),
                            result.getInt(21),
                            result.getInt(25),
                            result.getString(26),
                            result.getString(27)
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

}
