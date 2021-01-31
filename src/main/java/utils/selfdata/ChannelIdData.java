package utils.selfdata;

import com.fasterxml.jackson.annotation.*;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.entities.VoiceChannel;
import utils.tools.GTools;

public class ChannelIdData extends SavableSelfData {

    private static ChannelIdData data;

    private long suggestionChannelId;
    private long playerCountChannelId;
    private long prevSuggestHelpChannelId;
    private long raidAlertChannelId;
    private long modChannelId;
    private long privateChannelsCategoryId;

    @JsonCreator
    public ChannelIdData(@JsonProperty("suggestionChannelId") long suggestionChannelId,
                         @JsonProperty("playerCountChannelId") long playerCountChannelId,
                         @JsonProperty("prevSuggestHelpChannelId") long prevSuggestHelpChannelId,
                         @JsonProperty("raidAlertChannelId") long raidAlertChannelId,
                         @JsonProperty("modChannelId") long modChannelId,
                         @JsonProperty("privateChannelsCategoryId") long privateChannelsCategoryId) {
        super(Type.CHANNELID);
        this.suggestionChannelId = suggestionChannelId;
        this.playerCountChannelId = playerCountChannelId;
        this.prevSuggestHelpChannelId = prevSuggestHelpChannelId;
        this.raidAlertChannelId = raidAlertChannelId;
        this.modChannelId = modChannelId;
        this.privateChannelsCategoryId = privateChannelsCategoryId;
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
        return GTools.guild.getCategoryById(this.privateChannelsCategoryId);
    }

    @JsonSetter
    public void setPrivateChannelsCategoryId(long privateChannelsCategoryId) {
        this.privateChannelsCategoryId = privateChannelsCategoryId;
        this.save();
    }

    @JsonIgnore
    public static ChannelIdData get() {
        return data;
    }

    @JsonIgnore
    public static void load() {
        data = (ChannelIdData) SavableSelfData.obtainData(Type.CHANNELID);
        // default data
        if (data == null) {
            data = new ChannelIdData(0L, 0L, 0L, 0L, 0L, 0L);
            data.save();
        }
        System.out.println("Loaded Channel Id Data: " + data.toString());
    }
}
