package net.grandtheftmc.discordbot.utils.selfdata;

import com.fasterxml.jackson.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnnoyData extends SavableSelfData {

    private static AnnoyData data;

    // The first long is the user id, the second is the emoji id
    private Map<Long, String> emojiAnnoyMap = new HashMap<>();
    // The first long is the user id, and the array stores: long[0] = how often to send msg; long[1] = last annoyed time
    private Map<Long, Long[]> quoteAnnoyMap = new HashMap<>();
    // The first long is the user id, and the character is the character to replace their words with
    private Map<Long, Character> scrabbleAnnoyMap = new HashMap<>();
    // List of users ids being annoyed by bot annoy
    private List<Long> botAnnoyList = new ArrayList<>();

    @JsonCreator
    public AnnoyData(@JsonProperty("emojiAnnoyMap") Map<Long, String> emojiAnnoyMap,
                     @JsonProperty("quoteAnnoyMap") Map<Long, Long[]> quoteAnnoyMap,
                     @JsonProperty("scrabbleAnnoyMap") Map<Long, Character> scrabbleAnnoyMap,
                     @JsonProperty("botAnnoyList") List<Long> botAnnoyList) {
        super(Type.ANNOYDATA);
        this.emojiAnnoyMap = emojiAnnoyMap;
        this.quoteAnnoyMap = quoteAnnoyMap;
        this.scrabbleAnnoyMap = scrabbleAnnoyMap;
        this.botAnnoyList = botAnnoyList;
    }

    @JsonGetter
    public Map<Long, String> getEmojiAnnoyMap() {
        return emojiAnnoyMap;
    }

    @JsonGetter
    public Map<Long, Long[]> getQuoteAnnoyMap() {
        return quoteAnnoyMap;
    }

    @JsonGetter
    public Map<Long, Character> getScrabbleAnnoyMap() {
        return scrabbleAnnoyMap;
    }

    @JsonGetter
    public List<Long> getBotAnnoyList() {
        return botAnnoyList;
    }

    @JsonIgnore
    public static AnnoyData get() {
        return data;
    }

    @JsonIgnore
    public static void load() {
        data = (AnnoyData) obtainData(Type.ANNOYDATA);
        // default data
        if (data == null) {
            data = new AnnoyData(new HashMap<>(), new HashMap<>(), new HashMap<>(), new ArrayList<>());
            data.save();
        }
        System.out.println("Loaded Annoy Data: " + data.toString());
    }

}
