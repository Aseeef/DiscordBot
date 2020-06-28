package utils;

public class Suggestions {

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

    public String getMsg() {
        return msg;
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
