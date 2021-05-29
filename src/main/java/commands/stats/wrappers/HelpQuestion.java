package commands.stats.wrappers;

import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class HelpQuestion {

    /** Questions will will closed after this many seconds */
    public static final int CLOSE_SECONDS = 100;
    /** Questions answered after this many seconds will be counted as "late" */
    public static final int LATE_SECONDS = 50;

    /** The UUID of the player who asked the help question */
    protected final UUID askerUUID;
    /** The help question asked */
    protected final String question;
    /** The Core server the player was on when they asked the question */
    protected final String server;
    /** The time, in epoch millis at which the question was asked */
    protected final long askTime;
    /** List of the UUIDs of all staff members online when the question was asked */
    protected final List<UUID> onlineStaff;
    /** List of all answers to this help question in insertion order of first answered to last */
    protected LinkedList<HelpAnswer> answers;
    /** The reason this help question was closed */
    protected CloseReason closeReason;

    /** The unique id for this help question */
    protected int questionId = -1;

    public HelpQuestion(UUID askerUUID, String question, String server, long askTime, List<UUID> onlineStaff, LinkedList<HelpAnswer> answers, CloseReason closeReason) {
        this.askerUUID = askerUUID;
        this.question = question;
        this.server = server.toUpperCase();
        this.askTime = askTime;
        this.onlineStaff = onlineStaff;
        this.answers = answers;
        this.closeReason = closeReason;
    }

    public HelpQuestion(int questionId, UUID askerUUID, String question, String server, long askTime, List<UUID> onlineStaff, LinkedList<HelpAnswer> answers, CloseReason closeReason) {
        this(askerUUID, question, server, askTime, onlineStaff, answers, closeReason);
        this.questionId = questionId;
    }

    /**
     * @return Returns the DB question ID. If this question hasn't yet been inserted into the DB, it will return -1.
     */
    public int getQuestionId() {
        return questionId;
    }

    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }

    public final UUID getAskerUUID() {
        return askerUUID;
    }

    public final String getQuestion() {
        return question;
    }

    public final String getServer() {
        return server;
    }

    public final long getAskTime() {
        return askTime;
    }

    public List<UUID> getOnlineStaff() {
        return onlineStaff;
    }

    public LinkedList<HelpAnswer> getAnswers() {
        return answers;
    }

    public CloseReason getCloseReason() {
        return closeReason;
    }

    public HelpAnswer getAnswer (int index) {
        return this.answers.get(index);
    }

    /**
     * The the answer that was answered by specified staff
     *
     * @param staff - The answered, if made any by this staff. Specify null to get answer by Larry help bot.
     *
     * @return - The optional answer the specified staff gave to this question.
     */
    public Optional<HelpAnswer> getAnswer (@Nullable UUID staff) {
        return this.answers.stream().filter( (answer) -> {
            if (staff != null && answer != null) {
                return staff.equals(answer.getStaffUUID());
            } else return staff == null && answer.getStaffUUID() == null;
        }).findFirst();
    }

    public HelpAnswer getInitialAnswer() {
        return this.answers.size() > 0 ? getAnswer(0) : null;
    }

    public long getAnswerTime() {
        return this.getInitialAnswer() != null ? this.getInitialAnswer().replyTime : -1;
    }

    @Override
    public String toString() {
        return "HelpQuestion[askerUUID=" + this.askerUUID + ", question=" + this.question + ", askTime=" + this.askTime + ", answers=" + this.answers + "]";
    }

    public enum CloseReason {

        TIMEOUT(1),
        OVERRIDE(2),
        DISCONNECT(3),
        ;

        private int id;
        CloseReason(int id) {
            this.id = id;
        }

        public int getId() {
            return this.id;
        }

        public static CloseReason getReason(int id) {
            for (CloseReason cr : CloseReason.values()) {
                if (cr.id == id) {
                    return cr;
                }
            }
            return null;
        }

    }

}
