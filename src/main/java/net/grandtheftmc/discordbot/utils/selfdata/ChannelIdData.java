package net.grandtheftmc.discordbot.utils.selfdata;

import com.fasterxml.jackson.annotation.*;
import net.grandtheftmc.discordbot.GTMBot;
import net.dv8tion.jda.api.entities.Category;

public class ChannelIdData extends SavableSelfData {

    private static ChannelIdData data;

    private long suggestionChannelId = 0L;
    private long playerCountChannelId = 0L;
    private long prevSuggestHelpChannelId = 0L;
    private long raidAlertChannelId = 0L;
    private long modChannelId = 0L;
    private long privateChannelsCategoryId = 0L;
    private long bugReportChannelId = 0L;
    private long bugReceiveChannelId = 0L;

    public ChannelIdData() {
        super(Type.CHANNELID);
    }

    @JsonCreator
    public ChannelIdData(@JsonProperty("suggestionChannelId") long suggestionChannelId,
                         @JsonProperty("playerCountChannelId") long playerCountChannelId,
                         @JsonProperty("prevSuggestHelpChannelId") long prevSuggestHelpChannelId,
                         @JsonProperty("raidAlertChannelId") long raidAlertChannelId,
                         @JsonProperty("modChannelId") long modChannelId,
                         @JsonProperty("privateChannelsCategoryId") long privateChannelsCategoryId,
                         @JsonProperty("bugReportChannelId") long bugReportChannelId,
                         @JsonProperty("bugReceiveChannelId") long bugReceiveChannelId) {
        super(Type.CHANNELID);
        this.suggestionChannelId = suggestionChannelId;
        this.playerCountChannelId = playerCountChannelId;
        this.prevSuggestHelpChannelId = prevSuggestHelpChannelId;
        this.raidAlertChannelId = raidAlertChannelId;
        this.modChannelId = modChannelId;
        this.privateChannelsCategoryId = privateChannelsCategoryId;
        this.bugReportChannelId = bugReportChannelId;
        this.bugReceiveChannelId = bugReceiveChannelId;
    }

    @JsonGetter
    public static ChannelIdData getData() {
        return data;
    }

    @JsonSetter
    public static void setData(ChannelIdData data) {
        ChannelIdData.data = data;
    }

    @JsonGetter
    public long getSuggestionChannelId() {
        return suggestionChannelId;
    }

    @JsonSetter
    public void setSuggestionChannelId(long suggestionChannelId) {
        this.suggestionChannelId = suggestionChannelId;
        this.save();
    }

    @JsonGetter
    public long getPlayerCountChannelId() {
        return playerCountChannelId;
    }

    @JsonSetter
    public void setPlayerCountChannelId(long playerCountChannelId) {
        this.playerCountChannelId = playerCountChannelId;
        this.save();
    }

    @JsonGetter
    public long getPrevSuggestHelpChannelId() {
        return prevSuggestHelpChannelId;
    }

    @JsonSetter
    public void setPrevSuggestHelpChannelId(long prevSuggestHelpChannelId) {
        this.prevSuggestHelpChannelId = prevSuggestHelpChannelId;
        this.save();
    }

    @JsonGetter
    public long getRaidAlertChannelId() {
        return raidAlertChannelId;
    }

    @JsonSetter
    public void setRaidAlertChannelId(long raidAlertChannelId) {
        this.raidAlertChannelId = raidAlertChannelId;
        this.save();
    }

    @JsonGetter
    public long getModChannelId() {
        return modChannelId;
    }

    @JsonSetter
    public void setModChannelId(long modChannelId) {
        this.modChannelId = modChannelId;
        this.save();
    }

    @JsonGetter
    public long getPrivateChannelsCategoryId() {
        return privateChannelsCategoryId;
    }

    @JsonIgnore
    public Category getPrivateChannelsCategory() {
        return GTMBot.getGTMGuild().getCategoryById(this.privateChannelsCategoryId);
    }

    @JsonSetter
    public void setPrivateChannelsCategoryId(long privateChannelsCategoryId) {
        this.privateChannelsCategoryId = privateChannelsCategoryId;
        this.save();
    }

    @JsonGetter
    public long getBugReportChannelId() {
        return bugReportChannelId;
    }

    @JsonSetter
    public void setBugReportChannelId(long bugReportChannelId) {
        this.bugReportChannelId = bugReportChannelId;
        this.save();
    }

    @JsonGetter
    public long getBugReceiveChannelId() {
        return bugReceiveChannelId;
    }

    @JsonSetter
    public void setBugReceiveChannelId(long bugReceiveChannelId) {
        this.bugReceiveChannelId = bugReceiveChannelId;
        this.save();
    }

    @JsonIgnore
    public static ChannelIdData get() {
        return data;
    }

    @JsonIgnore
    public static void load() {
        data = (ChannelIdData) obtainData(Type.CHANNELID);
        // default data
        if (data == null) {
            data = new ChannelIdData();
            data.save();
        }
        System.out.println("Loaded Channel Id Data: " + data.toString());
    }
}
