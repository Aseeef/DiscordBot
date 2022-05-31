package net.grandtheftmc.discordbot.xenforo.objects.tickets;

import com.fasterxml.jackson.annotation.*;
import lombok.Getter;
import net.grandtheftmc.discordbot.xenforo.objects.TicketMessage;
import net.grandtheftmc.discordbot.xenforo.objects.XenforoUser;
import org.json.JSONObject;
import org.lorecraft.phparser.SerializedPhpParser;
import org.lorecraft.phparser.SerializedPhpParserException;
import net.grandtheftmc.discordbot.utils.console.Logs;
import net.grandtheftmc.discordbot.utils.database.XenforoDAO;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@JsonRootName("content") @JsonIgnoreProperties ({"guest_password", "is_primary_close", "last_message_id", "last_message_user_id", "last_message_username", "is_piping_email", "user_name"})
public class SupportTicket {

    private final int supportTicketId;
    private final String ticketId;
    private final String title;
    private final int userId;
    private final String username;
    private final String userEmail;
    private final int openingUserId;
    private final String openingUserName;
    private final int openDate;
    private final String urgency;
    private final int replyCount;
    private final int participantCount;
    private final int departmentId;
    private final int assignedUserId;
    private final int ticketStatusId;
    private final int firstMessageId;
    private final int lastUpdate;
    private final int lastMessageDate;
    private final int lastMessageId;
    private final float submittedRating;
    private final String ticketFields;
    private final String participants;


    @JsonCreator
    public SupportTicket(@JsonProperty("support_ticket_id") int supportTicketId,
                         @JsonProperty("ticket_id") String ticketId,
                         @JsonProperty("title") String title,
                         @JsonProperty("user_id") int userId,
                         @JsonProperty("username") String username,
                         @JsonProperty("user_email") String userEmail,
                         @JsonProperty("openner_user_id") int openingUserId,
                         @JsonProperty("openner_username") String openingUserName,
                         @JsonProperty("open_date") int openDate,
                         @JsonProperty("urgency") String urgency,
                         @JsonProperty("reply_count") int replyCount,
                         @JsonProperty("participant_count") int participantCount,
                         @JsonProperty("department_id") int departmentId,
                         @JsonProperty("assigned_user_id") int assignedUserId,
                         @JsonProperty("ticket_status_id") int ticketStatusId,
                         @JsonProperty("first_message_id") int firstMessageId,
                         @JsonProperty("last_update") int lastUpdate,
                         @JsonProperty("last_message_date") int lastMessageDate,
                         @JsonProperty("last_message_id") int lastMessageId,
                         @JsonProperty("submitter_rating") float submittedRating,
                         @JsonProperty("custom_support_ticket_fields") String ticketFields,
                         @JsonProperty("participants") String participants) {
        this.supportTicketId = supportTicketId;
        this.ticketId = ticketId;
        this.title = title;
        this.userId = userId;
        this.username = username;
        this.userEmail = userEmail;
        this.openingUserId = openingUserId;
        this.openingUserName = openingUserName;
        this.openDate = openDate;
        this.urgency = urgency;
        this.replyCount = replyCount;
        this.participantCount = participantCount;
        this.departmentId = departmentId;
        this.assignedUserId = assignedUserId;
        this.ticketStatusId = ticketStatusId;
        this.firstMessageId = firstMessageId;
        this.lastUpdate = lastUpdate;
        this.lastMessageDate = lastMessageDate;
        this.lastMessageId = lastMessageId;
        this.submittedRating = submittedRating;
        this.ticketFields = ticketFields;
        this.participants = participants;
    }

    @JsonIgnore
    public XenforoUser getUser() {
        return XenforoDAO.xenforoUserFromId(userId);
    }

    @JsonIgnore
    public XenforoUser getOpeningUser() {
        return XenforoDAO.xenforoUserFromId(openingUserId);
    }

    public Urgency getUrgency() {
        return Urgency.fromId(Integer.parseInt(urgency));
    }

    @JsonIgnore
    public Department getDepartment() {
        return Department.getDepartment(this.departmentId);
    }

    @JsonIgnore
    public XenforoUser getAssignedUser() {
        return XenforoDAO.xenforoUserFromId(assignedUserId);
    }

    @JsonIgnore
    public TicketStatus getTicketStatus() {
        return TicketStatus.fromId(ticketStatusId);
    }

    @JsonIgnore
    public TicketMessage getMessage(int messageId) {
        return XenforoDAO.getTicketMessage(this.supportTicketId, messageId);
    }

    @JsonIgnore
    public TicketMessage getLastMessage() {
        return XenforoDAO.getTicketMessage(this.supportTicketId, this.lastMessageId);
    }

    public JSONObject getTicketFields() {
        try {
            LinkedHashMap<Object, Object> ob = (LinkedHashMap<Object, Object>) new SerializedPhpParser(ticketFields).parse();
            JSONObject jsonObject = new JSONObject();
            ob.keySet().forEach(key -> jsonObject.put(String.valueOf(key), ob.get(key)));
            return new JSONObject(jsonObject.toString());
        } catch (SerializedPhpParserException e) {
            System.err.println("[SupportTicket] Unable to deserialize the following PHP String: " + this.ticketFields);
            e.printStackTrace();
            // sometimes, the php parser i made is still able to decipher php better than this
            return convertToMapOld(this.ticketFields);
        }
    }

    @JsonIgnore
    @Deprecated
    // Replaced with convertToMap which is much more stable and has array implementations
    // have to convert because this is the format we get for ticket fields...
    // a:3:{s:8:"username";s:8:"777kayoh";s:7:"servers";a:3:{s:10:"minesantos";s:10:"minesantos";s:9:"sanktburg";s:9:"sanktburg";s:12:"new_mineport";s:12:"new_mineport";}s:13:"transactionID";s:3:"N/A";}
    // a:4:{s:11:"insertproof";s:3:"Yes";s:5:"Abuse";s:3:"Yes";s:8:"username";s:12:"JustSkilz_NL";i:1993;s:6:"Snowwe";}
    private static JSONObject convertToMapOld(String string) {
        JSONObject fieldMap = new JSONObject();

        string = string.replaceFirst("a:[0-9]{1,2}:\\{", "")
                .replaceAll("[a-z]:[0-9]{1,2}:", "");
        string = string.substring(0, string.length()-1);

        Pattern p = Pattern.compile("\\{.*}");
        Matcher m = p.matcher(string);

        List<String> arrayData = new ArrayList<>();
        if (m.find()) {
            arrayData.add(m.group());
        }

        string = string.replaceAll("\\{.*}", "\"~%~\";") // place holder
                .replaceAll(";", "~@~"); // place holder
        String[] fields = string.split("~@~");

        int a = 0; // array data count
        for (int i = 0 ; i < fields.length ; i++) {
            if (i % 2 == 1) continue; // if i is odd continue

            if (i + 1 == fields.length) {
                Logs.log(fields[i], Logs.ERROR);
                break;
            }

            if (fields[i+1].equals("\"~%~\"")) {
                fieldMap.put(fields[i].replace("\"", ""), arrayData.get(a).replace("\"", ""));
                a++;
            } else fieldMap.put(fields[i].replace("\"", ""), fields[i+1].replace("\"", ""));
        }

        return fieldMap;
    }

    @JsonIgnore
    public String getTicketLink() {
        return new StringBuilder()
                .append("https://grandtheftmc.net/support-tickets/")
                .append(this.getTitle().replace(" ", "-").replaceAll("[^a-zA-Z0-9\\-]", ""))
                .append(".")
                .append(this.getSupportTicketId())
                .append("/")
                .toString();
    }

    @Override
    public String toString() {
        return this.ticketId + "[title=" + title + ", user=" + this.username + ", priority=" + this.getUrgency() + ", url=" + this.getTicketLink() + "]";
    }

    public enum Urgency {
        HIGH(1),
        MEDIUM(2),
        LOW(3),
        ;

        private final int id;
        Urgency (int id) {
            this.id = id;
        }

        public static Urgency fromId(int id) {
            for (Urgency value : values()) {
                if (value.id == id) {
                    return value;
                }
            }
            return MEDIUM;
        }
    }

    public enum TicketStatus {
        OPEN(1),
        ANSWERED(2),
        CUSTOMER_REPLY(3),
        CLOSED(4),
        AWAITING_CLIENT_RESPONSE(5)
        ;

        private final int id;
        TicketStatus (int id) {
            this.id = id;
        }

        public static TicketStatus fromId(int id) {
            for (TicketStatus value : values()) {
                if (value.id == id) {
                    return value;
                }
            }
            return CLOSED;
        }
    }
}
