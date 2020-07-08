package utils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import net.dv8tion.jda.api.entities.Category;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static utils.tools.GTools.guild;

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
    private long modChannelId;
    private long lastTicketRefreshTime;
    private long lastPostRefreshTime;
    // The first long is the user id, the second is the emoji id
    private Map<Long, String> emojiAnnoyMap = new HashMap<>();
    // The first long is the user id, and the array stores: long[0] = how often to send msg; long[1] = last annoyed time
    private Map<Long, Long[]> quoteAnnoyMap = new HashMap<>();
    // The first long is the user id, and the character is the character to replace their words with
    private Map<Long, Character> scrabbleAnnoyMap = new HashMap<>();
    private List<Long> botAnnoyList = new ArrayList<>();

    private long privateChannelsCategoryId;

    private static SelfData data;


    SelfData () {
    }

    SelfData (long suggestionChannelId, long playerCountChannelId, String previousBotName, long previousAvatarEdited,
              long prevSuggestHelpMsgId, long prevSuggestEmbedId, long prevSuggestHelpChannelId,
              long raidAlertChannelId,
              long ruleAgreeChannelId, long ruleAgreeMessageId,
              long modChannelId,
              long lastTicketRefreshTime, long lastPostRefreshTime,
              Map<Long, String> emojiAnnoyMap, Map<Long, Long[]> quoteAnnoyMap, Map<Long, Character> scrabbleAnnoyMap, List<Long> botAnnoyList,
              long privateChannelsCategoryId) {

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
        this.modChannelId = modChannelId;
        this.lastTicketRefreshTime = lastTicketRefreshTime;
        this.lastPostRefreshTime = lastPostRefreshTime;
        this.emojiAnnoyMap = emojiAnnoyMap;
        this.quoteAnnoyMap = quoteAnnoyMap;
        this.scrabbleAnnoyMap = scrabbleAnnoyMap;
        this.botAnnoyList = botAnnoyList;
        this.privateChannelsCategoryId = privateChannelsCategoryId;
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

    public long getModChannelId() {
        return modChannelId;
    }

    public void setModChannelId(long modChannelId) {
        this.modChannelId = modChannelId;
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

    public Map<Long, String> getEmojiAnnoyMap() {
        return emojiAnnoyMap;
    }

    public Map<Long, Long[]> getQuoteAnnoyMap() {
        return quoteAnnoyMap;
    }

    public Map<Long, Character> getScrabbleAnnoyMap() {
        return scrabbleAnnoyMap;
    }

    public List<Long> getBotAnnoyList() {
        return botAnnoyList;
    }

    public long getPrivateChannelsCategoryId() {
        return privateChannelsCategoryId;
    }

    @JsonIgnore
    public Category getPrivateChannelsCategory() {
        return guild.getCategoryById(privateChannelsCategoryId);
    }

    public void setPrivateChannelsCategory(long privateChannelsCategoryId) {
        this.privateChannelsCategoryId = privateChannelsCategoryId;
        Data.storeData(Data.SELFDATA, this);
    }

    public void update() {
        Data.storeData(Data.SELFDATA, this);
    }
}
