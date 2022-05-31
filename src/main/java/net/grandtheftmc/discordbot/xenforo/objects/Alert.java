package net.grandtheftmc.discordbot.xenforo.objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import net.grandtheftmc.discordbot.xenforo.objects.tickets.SupportTicket;

@Deprecated @Getter
@JsonIgnoreProperties ({"extra_data", "extra", "alerted_user_id", "new", "unviewed", "view_date"})
public class Alert {

    private final int alertId;
    private final String contentType;
    private final int contentId;
    private final String action;
    private final long eventDate;
    private final SupportTicket supportTicket;
    private final XenforoUserJson user;

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
