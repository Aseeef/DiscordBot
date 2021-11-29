package utils.database.redis;

import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import net.grandtheftmc.jedisnew.RedisEventListener;
import org.json.JSONObject;
import utils.Utils;
import utils.WebhookUtils;

import java.awt.*;

//todo finish
public class OnReceiveMessageStash implements RedisEventListener {

    @Override
    public String getChannel() {
        return "bitbucket";
    }

    @Override
    public void onRedisEvent(String s, JSONObject jsonObject) {

        if (!jsonObject.has("eventKey") || !(jsonObject.get("eventKey") instanceof String)) {
            System.err.println("[Debug] Received an unsupported message from BitBucket!");
            System.err.println(jsonObject);
            return;
        }

        EventType eventType = EventType.getEventType(jsonObject.getString("eventKey"));
        if (eventType == null) {
            System.err.println("[Debug] Received an unsupported Event Type from BitBucket!");
            System.err.println(jsonObject);
            return;
        }

        WebhookEmbedBuilder web = new WebhookEmbedBuilder();
        final String STASH_ICON = "https://img.favpng.com/5/6/10/bitbucket-portable-network-graphics-logo-github-repository-png-favpng-C52i9LPss9RJt5zsvs6EXNfpW.jpg";
        final String BASE_URL = "https://stash.grandtheftmc.net/projects/";

        System.out.println("[Debug] Received a " + eventType + " event from BitBucket!");
        System.out.println(jsonObject);

        JSONObject actor = jsonObject.getJSONObject("actor");
        String actorName = actor.getString("name");
        String actorEmail = actor.getString("emailAddress");

        switch (eventType) {

            case CODE_PUSHED: {
                JSONObject repository = jsonObject.getJSONObject("repository");
                JSONObject project = repository.getJSONObject("project");
                JSONObject changes = jsonObject.getJSONArray("changes").getJSONObject(0);


                web.setTitle(new WebhookEmbed.EmbedTitle("Incoming Commit(s) on " + repository.getString("repository") + "!", null));
                web.addField(new WebhookEmbed.EmbedField(true, "Project", project.getString("key") + "/" + repository.getString("name")));
                web.addField(new WebhookEmbed.EmbedField(true, "Branch", changes.getJSONObject("ref").getString("displayId")));
                web.addField(new WebhookEmbed.EmbedField(false, "User", actorName + " - " + actorEmail));
                web.addField(new WebhookEmbed.EmbedField(false, "Project URL", BASE_URL + project.getString("key") + "/repos/" + repository.getString("slug") + "/browse"));
                web.addField(new WebhookEmbed.EmbedField(false, "View Commits", BASE_URL + project.getString("key") + "/repos/" + repository.getString("slug") + "/commits?until=" + changes.getString("refId")));
                break;
            }
            case REPO_FORKED:
            case REPO_COMMENT:
            case REPO_MODIFIED:
            case PR_MERGED:
            case PR_OPENED:
            case PR_COMMENT:
            case PR_DELETED:
            case PR_APPROVED:
            case PR_DECLINED:
            case PR_NEEDS_WORK:
            case PR_UNAPPROVED:
                break;

        }

        web.setColor(eventType.getColor().getRGB());
        WebhookUtils.retrieveWebhookUrl(Utils.JDA.getTextChannelById(848055902249549834L)).thenAccept(url -> {
            WebhookUtils.sendMessage("BitBucket Stash", STASH_ICON, "", web.build(), url);
        });


    }

    enum EventType {
        //todo color code
        CODE_PUSHED("repo:refs_changed", Color.BLUE),
        REPO_MODIFIED("repo:modified", Color.YELLOW),
        REPO_FORKED("repo:forked", Color.YELLOW),
        REPO_COMMENT("repo:comment:added", Color.YELLOW),
        PR_OPENED("pr:opened", Color.GREEN),
        PR_APPROVED("pr:reviewer:approved", Color.GREEN),
        PR_UNAPPROVED("pr:reviewer:unapproved", Color.GREEN),
        PR_NEEDS_WORK("pr:reviewer:needs_work", Color.GREEN),
        PR_MERGED("pr:merged", Color.GREEN),
        PR_DECLINED("pr:declined", Color.GREEN),
        PR_DELETED("pr:deleted", Color.GREEN),
        PR_COMMENT("pr:comment:added", Color.GREEN),
        ;
        private final String eventKey;
        private final Color color;
        EventType(String eventKey, Color color) {
            this.eventKey = eventKey;
            this.color = color;
        }

        public String getEventKey() {
            return eventKey;
        }

        public Color getColor() {
            return color;
        }

        public static EventType getEventType(String key) {
            for (EventType eventType : EventType.values()) {
                if (eventType.eventKey.equals(key)) {
                    return eventType;
                }
            }
            return null;
        }
    }
}
