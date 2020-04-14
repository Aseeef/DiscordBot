package Utils;

public class SelfData {

    private long suggestionChannelId;
    private long playerCountChannelId;
    private String previousBotName;
    private long previousAvatarEdited;
    private long prevSuggestHelpMsgId;
    private long prevSuggestHelpChannelId;
    private long prevSuggestEmbedId;
    private long raidAlertChannelId;
    private long ruleAgreeChannelId;
    private long ruleAgreeMessageId;

    private static SelfData data;


    SelfData () {
    }

    SelfData (long suggestionChannelId, long playerCountChannelId, String previousBotName, long previousAvatarEdited,
              long prevSuggestHelpMsgId, long prevSuggestEmbedId, long prevSuggestHelpChannelId,
              long raidAlertChannelId,
              long ruleAgreeChannelId, long ruleAgreeMessageId) {

        this.suggestionChannelId = suggestionChannelId;
        this.playerCountChannelId = playerCountChannelId;
        this.previousBotName = previousBotName;
        this.previousAvatarEdited = previousAvatarEdited;
        this.prevSuggestHelpMsgId = prevSuggestHelpMsgId;
        this.prevSuggestEmbedId = prevSuggestEmbedId;
        this.prevSuggestHelpChannelId = prevSuggestHelpChannelId;
        this.raidAlertChannelId = raidAlertChannelId;
        this.ruleAgreeChannelId = ruleAgreeChannelId;
        this.ruleAgreeMessageId = ruleAgreeMessageId;

    }

    public static void load() {
        data = (SelfData) Data.obtainData(Data.SELFDATA);
    }

    public static SelfData get() {
        return data;
    }

    public long getSuggestionChannelId() {
        return suggestionChannelId;
    }

    public void setSuggestionChannelId(long suggestionChannelId) {
        this.suggestionChannelId = suggestionChannelId;
        Data.storeData(Data.SELFDATA, this);
    }

    public long getPlayerCountChannelId() {
        return playerCountChannelId;
    }

    public void setPlayerCountChannelId(long playerCountChannelId) {
        this.playerCountChannelId = playerCountChannelId;
        Data.storeData(Data.SELFDATA, this);
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

    public long getPrevSuggestHelpChannelId() {
        return prevSuggestHelpChannelId;
    }

    public void setPrevSuggestHelpChannelId(long prevSuggestHelpChannelId) {
        this.prevSuggestHelpChannelId = prevSuggestHelpChannelId;
        Data.storeData(Data.SELFDATA, this);
    }

    public long getRaidAlertChannelId() {
        return raidAlertChannelId;
    }

    public void setRaidAlertChannelId(long raidAlertChannelId) {
        this.raidAlertChannelId = raidAlertChannelId;
        Data.storeData(Data.SELFDATA, this);
    }

    public long getRuleAgreeChannelId() {
        return ruleAgreeChannelId;
    }

    public void setRuleAgreeChannelId(long ruleAgreeChannelId) {
        this.ruleAgreeChannelId = ruleAgreeChannelId;
        Data.storeData(Data.SELFDATA, this);
    }

    public long getRuleAgreeMessageId() {
        return ruleAgreeMessageId;
    }

    public void setRuleAgreeMessageId(long ruleAgreeMessageId) {
        this.ruleAgreeMessageId = ruleAgreeMessageId;
        Data.storeData(Data.SELFDATA, this);
    }

}
