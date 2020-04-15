package Utils;

import java.util.TimeZone;

public class Staff extends Users {

    private TimeZone timeZone;
    private String status;
    private long hired;
    private long left;

    public Staff(String uuid, String username, Rank rank, long discord, String timeZone, String status, long hired, long left) {
        super(uuid, username, rank, discord);
        this.timeZone = TimeZone.getTimeZone(timeZone);
        this.status = status;
        this.hired = hired;
        this.left = left;
    }

    public TimeZone getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getHired() {
        return hired;
    }

    public void setHired(long hired) {
        this.hired = hired;
    }

    public long getLeft() {
        return left;
    }

    public void setLeft(long left) {
        this.left = left;
    }
}
