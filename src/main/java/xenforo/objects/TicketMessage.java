package xenforo.objects;

import utils.database.XenforoDAO;

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

    public int getMessageId() {
        return messageId;
    }

    public int getTicketId() {
        return ticketId;
    }

    public int getMessageDate() {
        return messageDate;
    }

    public int getUserId() {
        return userId;
    }

    public XenforoUser getUser() {
        return XenforoDAO.xenforoUserFromId(userId);
    }

    public String getUsername() {
        return username;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getMessage() {
        return message;
    }
}
