package net.grandtheftmc.discordbot.commands.suggestions;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.grandtheftmc.discordbot.utils.Data;

public class Suggestions {

    private int number;
    // message id (for editing msg later)
    private long id;
    private String server;
    @JsonProperty("msg") private String suggestion;
    private String reason;
    private String suggesterId;
    private String status;
    private String statusReason;

    public Suggestions() {
    }

    @Deprecated
    public Suggestions(int number, String msg, String suggesterId, String status, String statusReason) {

        this.number = number;
        this.suggestion = msg;
        this.suggesterId = suggesterId;
        this.status = status;
        this.statusReason = statusReason;

        Data.storeData(Data.SUGGESTIONS, this, number);

    }

    public Suggestions(int number, String server, String suggestion, String reason, String suggesterId, String status, String statusReason) {
        this.number = number;
        this.server = server;
        this.suggestion = suggestion;
        this.reason = reason;
        this.suggesterId = suggesterId;
        this.status = status;
        this.statusReason = statusReason;

        Data.storeData(Data.SUGGESTIONS, this, number);
    }

    public int getNumber() {
        return number;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
        Data.storeData(Data.SUGGESTIONS, this, this.number);
    }

    public String getServer() {
        return server;
    }

    public String getSuggestion() {
        return suggestion;
    }

    public String getReason() {
        return reason;
    }

    public String getSuggesterId() {
        return suggesterId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status.toUpperCase();
        Data.storeData(Data.SUGGESTIONS, this, this.number);
    }

    public void setStatusReason(String statusReason) {
        this.statusReason = statusReason;
        Data.storeData(Data.SUGGESTIONS, this, this.number);
    }

    public String getStatusReason() {
        return statusReason;
    }

}
