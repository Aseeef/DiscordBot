package utils.selfdata;

import com.fasterxml.jackson.annotation.*;
import utils.channels.CustomChannel;

import java.util.HashMap;
import java.util.Map;

public class ChannelData extends SavableSelfData {

    private static ChannelData data;

    private HashMap<Long, CustomChannel> channelMap;

    @JsonCreator
    public ChannelData(@JsonProperty("channelMap") HashMap<Long, CustomChannel> channelMap) {
        super(Type.CHANNELDATA);
        this.channelMap = new HashMap<>();
        this.channelMap = channelMap;
    }

    @JsonGetter
    public Map<Long, CustomChannel> getChannelMap() {
        return channelMap;
    }

    @JsonSetter
    public static void setData(ChannelData data) {
        ChannelData.data = data;
    }

    @JsonIgnore
    public synchronized static ChannelData get() {
        return data;
    }

    @JsonIgnore
    public static void load() {
        data = (ChannelData) SavableSelfData.obtainData(Type.CHANNELDATA);
        // default data
        if (data == null) {
            data = new ChannelData(new HashMap<>());
            data.save();
        }
        System.out.println("Loaded Custom Channel Data: " + data.toString());
    }

}
