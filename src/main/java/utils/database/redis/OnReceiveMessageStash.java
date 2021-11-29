package utils.database.redis;

import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import net.grandtheftmc.jedisnew.RedisEventListener;
import org.json.JSONObject;
import utils.Utils;
import utils.WebhookUtils;

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
        System.out.println(jsonObject);

        JSONObject actor = jsonObject.getJSONObject("actor");
        String actorName = actor.getString("name");
        String actorEmail = actor.getString("emailAddress");

        switch (eventType) {

            case CODE_PUSHED: {
                JSONObject repository = jsonObject.getJSONObject("repository");
                JSONObject project = repository.getJSONObject("project");
                JSONObject changes = jsonObject.getJSONArray("changes").getJSONObject(0);
                web.setTitle(new WebhookEmbed.EmbedTitle("Incoming Commit(s) on " + project.getString("key") + "!", null));
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

        WebhookUtils.retrieveWebhookUrl(Utils.JDA.getTextChannelById(848055902249549834L)).thenAccept(url -> {
            WebhookUtils.sendMessage("BitBucket Stash", STASH_ICON, "", web.build(), url);
        });


    }

    /*
    .put(PULL_REQUEST_OPENED, "pullrequest:created")
.put(PULL_REQUEST_UPDATED, "pullrequest:updated")
.put(PULL_REQUEST_RESCOPED, "pullrequest:updated")
.put(PULL_REQUEST_REOPENED, "pullrequest:updated")
.put(PULL_REQUEST_MERGED, "pullrequest:fulfilled")
.put(PULL_REQUEST_DECLINED, "pullrequest:rejected")
.put(PULL_REQUEST_COMMENT, "pullrequest:comment")
.put(PULL_REQUEST_DELETED, "pullrequest:deleted")
.put(PULL_REQUEST_CANCELABLE_COMMENT, "pullrequest:comment")
.put(PULL_REQUEST_COMMENT_ACTIVITY, "pullrequest:comment")
.put(BUILD_STATUS_SET, "build:status")
.put(TAG_CREATED, "repo:push")
.put(BRANCH_CREATED, "repo:push")
.put(BRANCH_DELETED, "repo:push")
.put(REPOSITORY_MIRROR_SYNCHRONIZED, "repo:push")
.put(ABSTRACT_REPOSITORY_REFS_CHANGED, "repo:push")
     */

    enum EventType {
        //todo color code
        CODE_PUSHED("repo:refs_changed"),
        REPO_MODIFIED("repo:modified"),
        REPO_FORKED("repo:forked"),
        REPO_COMMENT("repo:comment:added"),
        PR_OPENED("pr:opened"),
        PR_APPROVED("pr:reviewer:approved"),
        PR_UNAPPROVED("pr:reviewer:unapproved"),
        PR_NEEDS_WORK("pr:reviewer:needs_work"),
        PR_MERGED("pr:merged"),
        PR_DECLINED("pr:declined"),
        PR_DELETED("pr:deleted"),
        PR_COMMENT("pr:comment:added"),

        ;
        private String eventKey;
        EventType(String eventKey) {
            this.eventKey = eventKey;
        }

        public String getEventKey() {
            return eventKey;
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
/*

REPO PUSH:

{
   "actor":{
      "displayName":"SkylixMC",
      "username":"SkylixMC"
   },
   "eventKey":"repo:push",
   "repository":{
      "owner":{
         "displayName":"GRAN",
         "username":"GRAN"
      },
      "public":false,
      "ownerName":"GRAN",
      "project":{
         "name":"GrandTheftMC",
         "key":"GRAN"
      },
      "fullName":"GRAN/discordbot",
      "links":{
         "self":[
            {
               "href":"https://stash.grandtheftmc.net/projects/GRAN/repos/d$scordbot/browse"
            }
         ]
      },
      "scmId":"git",
      "slug":"discordbot"
   },
   "push":{
      "changes":[
         {
            "new":{
               "name":"skylix_qa",
               "type":"branch",
               "target":{
                  "type":"commit",
                  "hash":"646897d01088c23a5fad3b87$981caa8a2dc8150"
               }
            },
            "created":false,
            "old":{
               "name":"skylix_qa",
               "type":"branch",
               "target":{
                  "type":"commit",
                  "hash":"ded665775aa15941cb3e886cb136637bd45f5245"
               }
            },
            "closed":false
         }
      ]
   }
}

-----------

PR UPDATED:
{
   "actor":{
      "displayName":"SkylixMC",
      "username":"SkylixMC"
   },
   "pullrequest":{
      "link":"https://stash.grandtheftmc.net/projects/GRAN/repos/discordbot/pull-requests/21",
      "authorLogin":"SkylixMC",
      "fromRef":{
         "commit":{
            "date":null,
            "authorTimestamp":0,
            "message":null,
            "hash":"646897d01088c23a5fad3b874981caa8a2dc8150"
         },
         "repository":{
            "owner":{
               "displayName":"GRAN",
               "username":"GRAN"
            },
            "public":false,
            "ownerName":"GRAN",
            "project":{
               "name":"GrandTheftMC",
               "key":"GRAN"
            },
            "fullName":"GRAN/discordbot",
            "links":{
               "self":[
                  {
                     "href":"https://stash.grandtheftmc.net/projects/GRAN/repos/discordbot/browse"
                  }
               ]
            },
            "scmId":"git",
            "slug":"discordbot"
         },
         "branch":{
            "rawNode":"646897d01088c23a5fad3b874981caa8a2dc8150",
            "name":"skylix_qa"
         }
      },
      "id":"21",
      "title":"Skylix qa",
      "toRef":{
         "commit":{
            "date":null,
            "authorTimestamp":0,
            "message":null,
            "hash":"e775be5fa0ab50bb3bba35ed95d9ee15207c9fbd"
         },
         "repository":{
            "owner":{
               "displayName":"GRAN",
               "username":"GRAN"
            },
            "public":false,
            "ownerName":"GRAN",
            "project":{
               "name":"GrandTheftMC",
               "key":"GRAN"
            },
            "fullName":"GRAN/discordbot",
            "links":{
               "self":[
                  {
                     "href":"https://stash.grandtheftmc.net/projects/GRAN/repos/discordbot/browse"
                  }
               ]
            },
            "scmId":"git",
            "slug":"discordbot"
         },
         "branch":{
            "rawNode":"e775be5fa0ab50bb3bba35ed95d9ee15207c9fbd",
            "name":"develop"
         }
      }
   },
   "eventKey":"pullrequest:updated",
   "repository":{
      "owner":{
         "displayName":"GRAN",
         "username":"GRAN"
      },
      "public":false,
      "ownerName":"GRAN",
      "project":{
         "name":"GrandTheftMC",
         "key":"GRAN"
      },
      "fullName":"GRAN/discordbot",
      "links":{
         "self":[
            {
               "href":"https://stash.grandtheftmc.net/projects/GRAN/repos/discordbot/browse"
            }
         ]
      },
      "scmId":"git",
      "slug":"discordbot"
   }
}


--------------

 */
