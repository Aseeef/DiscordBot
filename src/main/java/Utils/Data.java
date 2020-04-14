package Utils;

import Utils.tools.Logs;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;

import static Utils.tools.Logs.log;

public enum Data {
    SELFDATA("SELFDATA", SelfData.class),
    SUGGESTIONS("SUGGESTIONS", Suggestions.class);

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

    public static int getCurrentNumber(Data type) {
        return getNextNumber(type)-1;
    }

    public static boolean doesNumberExist (Data type, int number) {
        File file = new File("data/"+type.getDataName(), number+".json");
        return file.exists();
    }

    private static File createFile(Data type, int number) {

        File file = new File("data/" + type.getDataName(), number + ".json");

        try {
            if (file.createNewFile()) log("Creating a new " + type.getDataName() + " file named " + number + ".json");
        } catch (IOException e) {
            log(String.valueOf(e.initCause(e.getCause())), Logs.ERROR);
            for (StackTraceElement error : e.getStackTrace())
                log("        at " + error.toString(), Logs.ERROR);
        }

        return file;
    }

    public static void storeData(Data type, Object o) {

        int num = getCurrentNumber(type);

        createFile(type, num);
        storeData(type, o, num);

    }

    public static void storeData(Data type, Object o, int number) {
        createFile(type, number);

        // Create an ObjectMapper and serialize object to string for storage
        ObjectMapper om = new ObjectMapper();
        File file = new File("data/"+type.getDataName(), number+".json");

        try {
            om.writerWithDefaultPrettyPrinter().writeValue(file, o);
        } catch (IOException e) {
            log(String.valueOf(e.initCause(e.getCause())), Logs.ERROR);
            for (StackTraceElement error : e.getStackTrace())
                log("        at " + error.toString(), Logs.ERROR);
        }

        log("Edited " + type.getDataName() + " data in " + number + ".json");

    }

    public static Object obtainData(Data type, int number) {
        File file = new File("data/"+type.getDataName(), number+".json");

        // Load the object back
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(file, type.getaClass());
        } catch (IOException e) {
            log(String.valueOf(e.initCause(e.getCause())), Logs.ERROR);
            for (StackTraceElement error : e.getStackTrace())
                log("        at " + error.toString(), Logs.ERROR);
        }

        return null;
    }

    public static Object obtainData(Data type) {
        int num = getCurrentNumber(type);
        return obtainData(type, num);
    }

}
