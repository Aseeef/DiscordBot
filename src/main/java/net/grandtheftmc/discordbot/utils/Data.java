package net.grandtheftmc.discordbot.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.grandtheftmc.discordbot.commands.bugs.BugReport;
import net.grandtheftmc.discordbot.utils.console.Logs;
import net.grandtheftmc.discordbot.utils.users.GTMUser;
import net.grandtheftmc.discordbot.commands.suggestions.Suggestions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static net.grandtheftmc.discordbot.utils.console.Logs.log;

public enum Data {
    SUGGESTIONS("SUGGESTIONS", Suggestions.class),
    BUG_REPORTS("BUGS", BugReport.class),
    USER("USER", GTMUser.class),
    ;

    private final String dataName;
    private final Class aClass;

    Data(String dataName, Class aClass) {
        this.dataName = dataName;
        this.aClass = aClass;
    }

    public String getDataName() {
        return dataName;
    }

    public Class getaClass() {
        return aClass;
    }

    public static int getNextNumber(Data type) {
        int i = 0;
        File file = new File("data/"+type.getDataName(), i+".json");
        while (file.exists()) {
            i++;
            file = new File("data/"+type.getDataName(), i+".json");
        }
        return i;
    }

    public static long getCurrentNumber(Data type) {
        return getNextNumber(type)-1;
    }

    public static boolean doesDataExist(Data type, Object id) {
        File file = new File("data/"+type.getDataName(), id+".json");
        return file.exists();
    }

    private static File createFile(Data type, String number) {

        File directory = new File("data/" + type.getDataName());
        directory.mkdirs();
        File file = new File(directory, number + ".json");
        try {
            if (file.createNewFile()) Logs.log("Creating a new " + type.getDataName() + " file named " + number + ".json");
        } catch (IOException e) {
            Utils.printStackError(e);
        }

        return file;
    }

    public static void storeData(Data type, Object o) {

        long num = getCurrentNumber(type);

        createFile(type, String.valueOf(num));
        storeData(type, o, num);

    }

    public static void storeData(Data type, Object o, Object number) {
        createFile(type, number.toString());

        // Create an ObjectMapper and serialize object to string for storage
        ObjectMapper om = new ObjectMapper();
        File file = new File("data/"+type.getDataName(), number +".json");

        try {
            om.writerWithDefaultPrettyPrinter().writeValue(file, o);
        } catch (IOException e) {
            Utils.printStackError(e);
        }

        //log("Edited " + type.getDataName() + " data in " + number + ".json");

    }

    public static Object obtainData(Data type, Object number) {
        File file = new File("data/"+type.getDataName(), number+".json");

        // Load the object back
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(file, type.getaClass());
        } catch (IllegalStateException | IOException e ) {
            Utils.printStackError(e);
            System.err.println("Failed to load data id=" + number);
        }

        return null;
    }

    public static List<Object> getDataList(Data type) {
        File dir = new File("data", type.dataName);
        String[] dirArray = dir.list();

        List<Object> list = new ArrayList<>();
        if (dirArray != null && dirArray.length != 0)
            for (String fileName : dirArray) {
                if (new File(dir, fileName).isDirectory()) continue;
                fileName = fileName.replace(".json", "");
                list.add(Long.parseLong(fileName));
            }
        return list;
    }

    public static Object obtainData(Data type) {
        return obtainData(type, getCurrentNumber(type));
    }

    public static boolean deleteData(Data type, long number) {
        File file = new File("data/" + type.getDataName(), number + ".json");
        return file.delete();
    }

    public static boolean exists(Data type, long number) {
        File file = new File("data/" + type.getDataName(), number + ".json");
        return file.exists();
    }

}
