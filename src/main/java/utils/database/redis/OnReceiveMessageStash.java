package utils.database.redis;

import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import net.grandtheftmc.jedisnew.RedisEventListener;
import org.json.JSONObject;
import utils.Utils;
import utils.WebhookUtils;

import java.awt.*;

//TODO finish using https://confluence.atlassian.com/bitbucketserver059/event-payload-949255022.html
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

        String timestamp = jsonObject.getString("date");

        switch (eventType) {

            case CODE_PUSHED: {
                JSONObject repository = jsonObject.getJSONObject("repository");
                JSONObject project = repository.getJSONObject("project");
                JSONObject changes = jsonObject.getJSONArray("changes").getJSONObject(0);


                web.setTitle(new WebhookEmbed.EmbedTitle("Incoming Commit(s) on " + repository.getString("name") + "!", null));
                web.addField(new WebhookEmbed.EmbedField(true, "Project", project.getString("key") + "/" + repository.getString("name")));
                web.addField(new WebhookEmbed.EmbedField(true, "Branch", changes.getJSONObject("ref").getString("displayId")));
                web.addField(new WebhookEmbed.EmbedField(true, "User", actorName));
                web.addField(new WebhookEmbed.EmbedField(true, "Timestamp", timestamp));
                web.addField(new WebhookEmbed.EmbedField(false, "Project URL", BASE_URL + project.getString("key") + "/repos/" + repository.getString("slug") + "/browse"));
                web.addField(new WebhookEmbed.EmbedField(false, "View Commits", BASE_URL + project.getString("key") + "/repos/" + repository.getString("slug") + "/commits?until=" + changes.getString("refId")));
                break;
            }
            case REPO_FORKED: {

                break;
            }
            case REPO_COMMENT: {

                break;
            }
            case REPO_MODIFIED: {
                JSONObject oldData = jsonObject.getJSONObject("old");
                JSONObject newData = jsonObject.getJSONObject("new");

                web.setTitle(new WebhookEmbed.EmbedTitle("Repository Renamed!", null));
                web.addField(new WebhookEmbed.EmbedField(true, "From", oldData.getJSONObject("project").getString("key") + "/" + oldData.getString("name")));
                web.addField(new WebhookEmbed.EmbedField(true, "To", newData.getJSONObject("project").getString("key") + "/" + newData.getString("name")));
                web.addField(new WebhookEmbed.EmbedField(true, "User", actorName));
                web.addField(new WebhookEmbed.EmbedField(true, "Timestamp", timestamp));
                break;
            }
            case PR_MERGED: {

                break;
            }
            case PR_OPENED: {

                break;
            }
            case PR_COMMENT: {

                break;
            }
            case PR_DELETED: {

                break;
            }
            case PR_APPROVED: {

                break;
            }
            case PR_DECLINED: {

                break;
            }
            case PR_NEEDS_WORK: {

                break;
            }
            case PR_UNAPPROVED: {

                break;
            }

        }

        web.setColor(eventType.getColor().getRGB());
        WebhookUtils.retrieveWebhookUrl(Utils.JDA.getTextChannelById(848055902249549834L)).thenAccept(url -> {
            WebhookUtils.sendMessage("BitBucket Stash", STASH_ICON, "", web.build(), url);
        });


    }

    enum EventType {
        CODE_PUSHED("repo:refs_changed", Color.BLUE),
        REPO_MODIFIED("repo:modified", Color.YELLOW),
        REPO_FORKED("repo:forked", Color.YELLOW),
        REPO_COMMENT("repo:comment:added", Color.YELLOW),
        PR_OPENED("pr:opened", Color.RED),
        PR_APPROVED("pr:reviewer:approved", Color.RED),
        PR_UNAPPROVED("pr:reviewer:unapproved", Color.RED),
        PR_NEEDS_WORK("pr:reviewer:needs_work", Color.RED),
        PR_MERGED("pr:merged", Color.RED),
        PR_DECLINED("pr:declined", Color.RED),
        PR_DELETED("pr:deleted", Color.RED),
        PR_COMMENT("pr:comment:added", Color.RED),
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
                if (eventType.getEventKey().equals(key)) {
                    return eventType;
                }
            }
            return null;
        }
    }
}
