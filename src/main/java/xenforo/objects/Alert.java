package xenforo.objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import xenforo.objects.tickets.SupportTicket;

@Deprecated @Getter
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

}
