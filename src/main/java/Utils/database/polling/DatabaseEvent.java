package Utils.database.polling;

import Utils.Rank;
import Utils.tools.Logs;
import Utils.tools.Verification;
import org.json.JSONObject;

import java.util.UUID;
public class DatabaseEvent {

    private UUID uuid;
    private String name;
    private String action;
    private JSONObject jsonData;

    public DatabaseEvent(UUID uuid, String name, String action, String data) {
        this.uuid = uuid;
        this.name = name;
        this.action = action;
        this.jsonData = new JSONObject(data);
        onDatabaseEvent();
    }

    /** This is the logic that goes on when there is a database event */
    private void onDatabaseEvent() {

        switch (action.toLowerCase()) {

            case "verify":
                Rank rank = Rank.getRankFromString(jsonData.getString("rank"));
                String verifyCode = jsonData.getString("code");
                if (rank == null) return;
                Verification.createVerifyRequest(verifyCode, uuid, name, rank);
                break;

            case "disconnect":

                break;

            default:
                Logs.log("[DEBUG] [DatabaseEvent] Received unknown action request from GTM: " + action);
                break;


        }


    }

}
