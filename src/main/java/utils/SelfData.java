package utils;

@Deprecated
/**
 * @deprecated Please create a new class extending SavableSelfData from onwards.
 * This is pretty messy and should be avoided.
 */
public class SelfData {

    private String previousBotName;
    private long previousAvatarEdited;
    private long prevSuggestHelpMsgId;
    private long prevSuggestEmbedId;
    private long ruleAgreeMessageId;
    private long lastTicketRefreshTime;
    private long lastPostRefreshTime;

    private static SelfData data;


    SelfData () {
    }

    SelfData (String previousBotName, long previousAvatarEdited,
              long prevSuggestHelpMsgId, long prevSuggestEmbedId,
              long ruleAgreeMessageId,
              long lastTicketRefreshTime, long lastPostRefreshTime) {

        this.previousBotName = previousBotName;
        this.previousAvatarEdited = previousAvatarEdited;
        this.prevSuggestHelpMsgId = prevSuggestHelpMsgId;
        this.prevSuggestEmbedId = prevSuggestEmbedId;
        this.ruleAgreeMessageId = ruleAgreeMessageId;
        this.lastTicketRefreshTime = lastTicketRefreshTime;
        this.lastPostRefreshTime = lastPostRefreshTime;
    }

    public static void load() {
        data = (SelfData) Data.obtainData(Data.SELFDATA);
    }

    public static SelfData get() {
        return data;
    }

    public String getPreviousBotName() {
        return previousBotName;
    }

    public void setPreviousBotName(String previousBotName) {
        this.previousBotName = previousBotName;
        Data.storeData(Data.SELFDATA, this);
    }

    public long getPreviousAvatarEdited() {
        return previousAvatarEdited;
    }

    public void setPreviousAvatarEdited(long previousAvatarEdited) {
        this.previousAvatarEdited = previousAvatarEdited;
        Data.storeData(Data.SELFDATA, this);
    }

    public long getPrevSuggestHelpMsgId() {
        return prevSuggestHelpMsgId;
    }

    public void setPrevSuggestHelpMsgId(long prevSuggestHelpMsgId) {
        this.prevSuggestHelpMsgId = prevSuggestHelpMsgId;
        Data.storeData(Data.SELFDATA, this);
    }

    public long getPrevSuggestEmbedId() {
        return prevSuggestEmbedId;
    }

    public void setPrevSuggestEmbedId(long prevSuggestEmbedId) {
        this.prevSuggestEmbedId = prevSuggestEmbedId;
        Data.storeData(Data.SELFDATA, this);
    }

    public long getRuleAgreeMessageId() {
        return ruleAgreeMessageId;
    }

    public void setRuleAgreeMessageId(long ruleAgreeMessageId) {
        this.ruleAgreeMessageId = ruleAgreeMessageId;
        Data.storeData(Data.SELFDATA, this);
    }

    public long getLastTicketRefreshTime() {
        return lastTicketRefreshTime;
    }

    public void setLastTicketRefreshTime(long lastTicketRefreshTime) {
        this.lastTicketRefreshTime = lastTicketRefreshTime;
        Data.storeData(Data.SELFDATA, this);
    }

    public long getLastPostRefreshTime() {
        return lastPostRefreshTime;
    }

    public void setLastPostRefreshTime(long lastPostRefreshTime) {
        this.lastPostRefreshTime = lastPostRefreshTime;
        Data.storeData(Data.SELFDATA, this);
    }

}
