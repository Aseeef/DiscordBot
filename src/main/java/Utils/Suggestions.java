package Utils;

import java.io.IOException;
import java.io.Serializable;

public class Suggestions implements Serializable {

    private int number;
    private long id;
    private String msg;
    private String suggesterId;
    private String status;
    private String statusReason;

    public Suggestions() {
    }

    public Suggestions(int number, String msg, String suggesterId, String status, String statusReason) {

        this.number = number;
        this.msg = msg;
        this.suggesterId = suggesterId;
        this.status = status;
        this.statusReason = statusReason;

        try {
            Data.storeData(Data.SUGGESTIONS, this, number);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public int getNumber() {
        return number;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) throws IOException {
        this.id = id;
        Data.storeData(Data.SUGGESTIONS, this, this.number);
    }

    public String getMsg() {
        return msg;
    }

    public String getSuggesterId() {
        return suggesterId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) throws IOException {
        this.status = status.toUpperCase();
        Data.storeData(Data.SUGGESTIONS, this, this.number);
    }

    public void setStatusReason(String statusReason) throws IOException {
        this.statusReason = statusReason;
        Data.storeData(Data.SUGGESTIONS, this, this.number);
    }

    public String getStatusReason() {
        return statusReason;
    }

}
