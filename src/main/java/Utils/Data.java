package Utils;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;

public enum Data {
    SUGGESTIONS("SUGGESTIONS"), REPORTS("REPORTS");

    Data(String dataType) {
    }

    public static int getNextNumber(Data type) {
        int i = 0;
        File file = new File("data/"+type, i+".json");
        while (file.exists()) {
            i++;
            file = new File("data/"+type, i+".json");
        }
        return i;
    }

    public static int getCurrentNumber(Data type) {
        return getNextNumber(type)-1;
    }

    public static boolean doesNumberExist (Data type, int number) {
        File file = new File("data/"+type, number+".json");
        return file.exists();
    }

    private static File createFile(Data type) throws IOException {
        File file = new File("data/"+type, getNextNumber(type)+".json");
        if (file.createNewFile()) System.out.println("New file created");
        else System.out.println("Error");

        return file;
    }

    public static void storeData(Data type, Suggestions s) throws IOException {
        createFile(type);

        // Create an ObjectMapper and serialize object to string for storage
        ObjectMapper om = new ObjectMapper();
        File file = new File("data/"+type, getCurrentNumber(type)+".json");

        om.writerWithDefaultPrettyPrinter().writeValue(file, s);
    }

    public static void storeData(Data type, Suggestions s, int number) throws IOException {
        // Create an ObjectMapper and serialize object to string for storage
        ObjectMapper om = new ObjectMapper();
        File file = new File("data/"+type, number+".json");

        om.writerWithDefaultPrettyPrinter().writeValue(file, s);
    }

    public static Suggestions obtainData(Data type, int number) throws IOException, ClassNotFoundException {
        File file = new File("data/"+type, number+".json");

        // Load the object back
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(file, Suggestions.class);
    }

}
