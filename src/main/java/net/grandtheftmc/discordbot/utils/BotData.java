package net.grandtheftmc.discordbot.utils;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public enum BotData {

    // Note: Data type must only contain default java class or primative classes list ArrayList, HashMap, Long, String, etc...
    LAST_BOT_NAME("Harry"),
    AVATAR_LAST_EDIT( 1628887011000L),
    //LAST_SUGGEST_MSG_ID( 0L),
    LAST_SUGGEST_EMBED_ID( 0L),
    LAST_BUG_MSG_ID( 0L),
    LAST_BUG_EMBED_ID( 0L),
    LAST_TICKET_REFRESH(1628887011000L),
    LAST_CLICKUP_REFRESH(1628887011000L),
    // string is bug id, long is insert time
    CLICKUP_PENDING_BUGS(new HashMap<String, Long>()),
    CLICKUP_TO_IGNORE(new HashMap<String, Long>()),
    // TODO: 3 keys - 1. Project 2. Repo 3. Branch value: last known commit
    LAST_COMMIT_POLL(new HashMap<String, String>());

    private static HashMap<String, Object> dataList = new HashMap<>();
    private static final File FILE = new File("data/BotData.json");

    private final Object defaultValue;

    BotData(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    public Object getData() {
        if (dataList.containsKey(this.toString())) {
            return dataList.get(this.toString());
        } else {
            dataList.put(this.toString(), this.defaultValue);
            return this.defaultValue;
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T getData(Class<T> expectedClass) {
        if (dataList.containsKey(this.toString())) {
            Object data = dataList.get(this.toString());
            if (expectedClass == Long.TYPE || expectedClass == Long.class) {
                data = ((Number) data).longValue();
            }
            return  (T) data;
        } else {
            dataList.put(this.toString(), this.defaultValue);
            return (T) this.defaultValue;
        }
    }

    public void setValue(Object value) {
        dataList.put(this.toString(), value);
        save();
    }

    public static void load() {

        if (FILE.exists()) {
            // Load the object back
            try {
                ObjectMapper mapper = new ObjectMapper();
                dataList = mapper.readValue(FILE, HashMap.class);
            } catch (IllegalStateException | IOException | NullPointerException e) {
                Utils.printStackError(e);
            }
        } else {
            for (BotData data : BotData.values()) {
                dataList.put(data.toString(), data.defaultValue);
            }
        }

        save();
        System.out.println("Loaded bot data: " + dataList);
    }

    public static void save() {
        try {
            if (!FILE.exists()) {
                FILE.getParentFile().mkdirs();
                FILE.createNewFile();
            }

            // Create an ObjectMapper and serialize object to string for storage
            ObjectMapper om = new ObjectMapper();
            om.writerWithDefaultPrettyPrinter().writeValue(FILE, dataList);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
