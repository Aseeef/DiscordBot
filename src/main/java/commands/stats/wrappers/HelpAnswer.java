package commands.stats.wrappers;

import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;
import utils.UUIDUtil;

import java.util.LinkedList;
import java.util.UUID;

public class HelpAnswer {

    @Nullable private final UUID staffUUID;
    private final String answer;
    final long replyTime;

    /**
     *
     * @param staffUUID - The UUID of the staff member who answered this question. If the UUID is null, the displayed staff member will be "Larry".
     * @param answer - The Answer the staff gave
     * @param replyTime - The epoch time this staff answered at
     */
    public HelpAnswer(@Nullable UUID staffUUID, String answer, long replyTime) {
        this.staffUUID = staffUUID;
        this.answer = answer;
        this.replyTime = replyTime;
    }

    @Nullable
    public UUID getStaffUUID() {
        return staffUUID;
    }

    public String getAnswer() {
        return answer;
    }

    public long getReplyTime() {
        return replyTime;
    }

    @Override
    public String toString() {
        return "HelpAnswer[staffUUID=" + this.staffUUID + ", staffName=" + staffUUID + ", answer=" + this.answer + ", replyTime=" + replyTime + "]";
    }

    public String toJsonString() {
        JSONObject jo = new JSONObject()
                .put("staff", this.staffUUID == null ? "LARRY" : this.staffUUID.toString().replaceAll("-", ""))
                .put("answer", this.answer)
                .put("replyTime", this.replyTime);
        return jo.toString();
    }

    public static HelpAnswer deserializeHelpAnswer(String serializedHelpAnswer) {
        JSONObject jo = new JSONObject(serializedHelpAnswer);

        return new HelpAnswer((jo.getString("staff").equals("LARRY") ? null : UUIDUtil.createUUID(jo.getString("staff")).orElse(null)),
                jo.getString("answer"), jo.getLong("replyTime"));
    }

    public static LinkedList<HelpAnswer> deserializeAnswersList(String serializedArray) {
        LinkedList<HelpAnswer> helpAnswers = new LinkedList<>();

        JSONArray ja = new JSONArray(serializedArray);
        for (int i = 0 ; i < ja.length() ; i++) {
            helpAnswers.add(deserializeHelpAnswer(ja.getString(i)));
        }

        return helpAnswers;
    }

}
