package net.grandtheftmc.discordbot.utils.selfdata;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.grandtheftmc.discordbot.utils.Utils;

import java.io.File;
import java.io.IOException;

import static net.grandtheftmc.discordbot.utils.console.Logs.log;

public abstract class SavableSelfData {

    public enum Type {
        CHANNELDATA("channels", ChannelData.class),
        ANNOYDATA("annoy", AnnoyData.class),
        CHANNELID("channel_ids", ChannelIdData.class),
        ;

        private final String name;
        private final Class<? extends SavableSelfData> aClass;


        Type(String name, Class<? extends SavableSelfData> aClass) {
            this.name = name;
            this.aClass = aClass;
        }

    }

    private final Type dataType;

    @JsonIgnore
    public SavableSelfData(Type dataType) {
        this.dataType = dataType;
    }

    @JsonIgnore
    public void save() {
        if (this.createData())
            log("Creating a new SELFDATA named " + this.dataType.name + ".json");

        // Create an ObjectMapper and serialize object to string for storage
        ObjectMapper om = new ObjectMapper();
        File file = new File("data/SELFDATA", this.dataType.name+".json");

        try {
            om.writerWithDefaultPrettyPrinter().writeValue(file, this);
        } catch (IOException e) {
            Utils.printStackError(e);
        }

        //debug
        //log("Edited " + type.getDataName() + " data in " + number + ".json");
    }

    @JsonIgnore
    public String toString() {
        try {
            // Create an ObjectMapper
            ObjectMapper om = new ObjectMapper();
            // Serialize to string
            return om.writer().writeValueAsString(this);
        } catch (JsonProcessingException ex) {
            ex.printStackTrace();
            return super.toString();
        }
    }

    @JsonIgnore
    private boolean createData() {
        return createData(this.dataType);
    }

    @JsonIgnore
    private static boolean createData(Type dataType) {

        File file = new File("data/SELFDATA", dataType.name+".json");

        try {
            return file.createNewFile();
        } catch (IOException e) {
            Utils.printStackError(e);
        }

        return false;
    }

    @JsonIgnore
    public SavableSelfData obtainData() {
        return obtainData(this.dataType);
    }

    @JsonIgnore
    public static SavableSelfData obtainData(Type dataType) {
        File file = new File("data/SELFDATA", dataType.name+".json");

        if (createData(dataType))
            log("Creating a new SELFDATA named " + dataType.name+".json");

        // Load the object back
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(file, dataType.aClass);
        } catch (IllegalStateException | IOException | NullPointerException e ) {
            Utils.printStackError(e);
        }

        return null;
    }

}
