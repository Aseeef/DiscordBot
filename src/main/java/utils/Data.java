package utils;

import utils.tools.GTools;
import utils.users.GTMUser;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static utils.console.Logs.log;

public enum Data {
    SELFDATA("SELFDATA", SelfData.class),
    SUGGESTIONS("SUGGESTIONS", Suggestions.class),
    USER("USER", GTMUser.class),
    ;

    private String dataName;
    private Class aClass;

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

    public static boolean doesNumberExist (Data type, int number) {
        File file = new File("data/"+type.getDataName(), number+".json");
        return file.exists();
    }

    private static File createFile(Data type, long number) {

        File file = new File("data/" + type.getDataName(), number + ".json");

        try {
            if (file.createNewFile()) log("Creating a new " + type.getDataName() + " file named " + number + ".json");
        } catch (IOException e) {
            GTools.printStackError(e);
        }

        return file;
    }

    public static void storeData(Data type, Object o) {

        long num = getCurrentNumber(type);

        createFile(type, num);
        storeData(type, o, num);

    }

    public static void storeData(Data type, Object o, long number) {
        createFile(type, number);

        // Create an ObjectMapper and serialize object to string for storage
        ObjectMapper om = new ObjectMapper();
        File file = new File("data/"+type.getDataName(), number+".json");

        try {
            om.writerWithDefaultPrettyPrinter().writeValue(file, o);
        } catch (IOException e) {
            GTools.printStackError(e);
        }

        //log("Edited " + type.getDataName() + " data in " + number + ".json");

    }

    public static Object obtainData(Data type, long number) {
        File file = new File("data/"+type.getDataName(), number+".json");

        // Load the object back
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(file, type.getaClass());
        } catch (IllegalStateException | IOException e ) {
            GTools.printStackError(e);
        }

        return null;
    }

    public static List<Long> getDataList(Data type) {
        File dir = new File("data");
        String[] dirArray = dir.list();
        if (dirArray == null || dirArray.length == 0) return null;

        List<Long> list = new ArrayList<>();
        for (String fileName : dirArray) {
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
