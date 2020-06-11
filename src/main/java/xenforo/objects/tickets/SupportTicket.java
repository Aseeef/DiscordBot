package xenforo.objects.tickets;

import Utils.console.Logs;
import com.fasterxml.jackson.annotation.*;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@JsonRootName("content") @JsonIgnoreProperties ({"guest_password", "is_primary_close", "last_message_id", "last_message_user_id", "last_message_username", "is_piping_email", "user_name"})
public class SupportTicket {

    private int supportTicketId;
    private String ticketId;
    private String title;
    private int userId;
    private String username;
    private String userEmail;
    private int openingUserId;
    private String openingUserName;
    private int openDate;
    private String urgency;
    private int replyCount;
    private int participantCount;
    private int departmentId;
    private int assignedUserId;
    private int ticketStatusId;
    private int firstMessageId;
    private long lastUpdate;
    private long lastMessageDate;
    private int submittedRating;
    private String ticketFields;
    private String participants;


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
                         @JsonProperty("last_update") long lastUpdate,
                         @JsonProperty("last_message_date") long lastMessageDate,
                         @JsonProperty("submitter_rating") int submittedRating,
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
        this.submittedRating = submittedRating;
        this.ticketFields = ticketFields;
        this.participants = participants;
    }

    public int getSupportTicketId() {
        return supportTicketId;
    }

    public String getTicketId() {
        return ticketId;
    }

    public String getTitle() {
        return title;
    }

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public int getOpeningUserId() {
        return openingUserId;
    }

    public String getOpeningUserName() {
        return openingUserName;
    }

    public int getOpenDate() {
        return openDate;
    }

    public String getUrgency() {
        return urgency;
    }

    public int getReplyCount() {
        return replyCount;
    }

    public int getParticipantCount() {
        return participantCount;
    }

    public int getDepartmentId() {
        return departmentId;
    }

    public int getAssignedUserId() {
        return assignedUserId;
    }

    public int getTicketStatusId() {
        return ticketStatusId;
    }

    public int getFirstMessageId() {
        return firstMessageId;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    public long getLastMessageDate() {
        return lastMessageDate;
    }

    public int getSubmittedRating() {
        return submittedRating;
    }

    public JSONObject getTicketFields() {
        return convertToMap(ticketFields);
    }

    public String getParticipants() {
        return participants;
    }

    @JsonIgnore
    // have to convert because this is the format we get for ticket fields...
    // a:3:{"username";"777kayoh";"servers";a:3:{"minesantos";"minesantos";"sanktburg";"sanktburg";"new_mineport";"new_mineport";}"transactionID";"N/A";}
    // a:4:{s:11:"insertproof";s:3:"Yes";s:5:"Abuse";s:3:"Yes";s:8:"username";s:12:"JustSkilz_NL";i:1993;s:6:"Snowwe";}
    private static JSONObject convertToMap(String string) {
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
}
