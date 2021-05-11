package xenforo.objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import xenforo.objects.tickets.SupportTicket;

@Deprecated
@JsonIgnoreProperties ({"extra_data", "extra", "alerted_user_id", "new", "unviewed", "view_date"})
public class Alert {

    private int alertId;
    private String contentType;
    private int contentId;
    private String action;
    private long eventDate;
    private SupportTicket supportTicket;
    private XenforoUserJson user;

    @JsonCreator
    public Alert(@JsonProperty("alert_id") int alertId,
                 @JsonProperty("content_type") String contentType,
                 @JsonProperty("content_id") int contentId,
                 @JsonProperty("action") String action,
                 @JsonProperty("event_date") long eventDate,
                 @JsonProperty("content") SupportTicket supportTicket,
                 @JsonProperty("user") XenforoUserJson user) {
        this.alertId = alertId;
        this.contentType = contentType;
        this.contentId = contentId;
        this.action = action;
        this.eventDate = eventDate;
        this.supportTicket = supportTicket;
        this.user = user;
    }

    public int getAlertId() {
        return alertId;
    }

    public String getContentType() {
        return contentType;
    }

    public int getContentId() {
        return contentId;
    }

    public String getAction() {
        return action;
    }

    public long getEventDate() {
        return eventDate;
    }

    public SupportTicket getSupportTicket() {
        return supportTicket;
    }

    public XenforoUserJson getUser() {
        return user;
    }

}
