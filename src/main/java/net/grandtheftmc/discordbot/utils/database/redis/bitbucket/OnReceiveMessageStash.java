package net.grandtheftmc.discordbot.utils.database.redis.bitbucket;

import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import lombok.SneakyThrows;
import net.grandtheftmc.discordbot.GTMBot;
import net.grandtheftmc.discordbot.utils.Utils;
import net.grandtheftmc.jedisnew.RedisEventListener;
import org.json.JSONObject;
import net.grandtheftmc.discordbot.utils.WebhookUtils;
import net.grandtheftmc.discordbot.utils.database.redis.bitbucket.wrappers.Commit;

import java.awt.*;
import java.util.List;

//TODO finish using https://confluence.atlassian.com/bitbucketserver059/event-payload-949255022.html
@Deprecated
public class OnReceiveMessageStash implements RedisEventListener {

    @Override
    public String getChannel() {
        return "bitbucket";
    }

    @SneakyThrows
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
                JSONObject repo = jsonObject.getJSONObject("repository");
                String projKey = repo.getJSONObject("project").getString("key");
                String repoSlug = repo.getString("slug");
                int authorId = actor.getInt("id");
                for (Object change : jsonObject.getJSONArray("changes")) {
                    String refId = ((JSONObject) change).getString("refId");
                    List<Commit> commits = new BitbucketPushes(projKey, repoSlug, authorId, refId).getCommits();
                    web.setTitle(new WebhookEmbed.EmbedTitle(commits.size() + " incoming Commit(s) on [" + projKey + "/" + repoSlug + "]!", null));
                    web.addField(new WebhookEmbed.EmbedField(true, "Project", repo.getJSONObject("project").getString("name")));
                    web.addField(new WebhookEmbed.EmbedField(true, "Repository", repoSlug));
                    web.addField(new WebhookEmbed.EmbedField(true, "Branch", refId));
                    web.addField(new WebhookEmbed.EmbedField(true, "User", actorName));
                    web.addField(new WebhookEmbed.EmbedField(true, "Timestamp", timestamp));
                    for (Commit commit : commits) {
                        web.addField(new WebhookEmbed.EmbedField(false, commit.getDisplayId(), "`" + commit.getMessage() + "`"));
                    }

                    System.out.println(web);
                    System.out.println(jsonObject.getJSONArray("changes"));
                    System.out.println(commits);
                }

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
        WebhookUtils.retrieveWebhookUrl(GTMBot.getJDA().getTextChannelById(848055902249549834L)).thenAccept(url -> {
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
