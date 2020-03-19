package Utils.AutoDeleter;

import net.dv8tion.jda.api.entities.Message;

import java.util.ArrayList;

public class DeleteMe {

    private static ArrayList<String> msgs = new ArrayList<>();

    /**
     * Due the msg to be deleted after a bit.
     * Note: Must queue before actually sending the msg
     * @param msg - The msg which should be deleted
     */
    public static void deleteQueue (Message msg) {
        msgs.add(msg.getContentRaw());
    }

    /**
     * Due the msg to be deleted after a bit.
     * Note: Must queue before actually sending the msg
     * @param msg - The msg which should be deleted
     */
    public static void deleteQueue (String msg) {
        msgs.add(msg);
    }

    public static boolean contains(String msg) {
        if (msgs.contains(msg)) {
            msgs.remove(msg);
            return true;
        }
        else
            return false;
    }

}
