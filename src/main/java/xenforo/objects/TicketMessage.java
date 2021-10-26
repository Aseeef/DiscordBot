package xenforo.objects;

import lombok.Getter;
import utils.database.XenforoDAO;

@Getter
public class TicketMessage {

    int messageId;
    int ticketId;
    int messageDate;
    int userId;
    String username;
    String userEmail;
    String message;

    public TicketMessage(int messageId, int ticketId, int messageDate, int userId, String username, String userEmail, String message) {
        this.messageId = messageId;
        this.ticketId = ticketId;
        this.messageDate = messageDate;
        this.userId = userId;
        this.username = username;
        this.userEmail = userEmail;
        this.message = message;
    }

    public XenforoUser getUser() {
        return XenforoDAO.xenforoUserFromId(userId);
    }

}
