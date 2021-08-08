package events;

import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import utils.webhooks.WebhookUtils;

import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is basically a message parser. We use a disgusting work around where:
 * Stash/Jenkins send msg to Slack.
 * Zapier sends messages from Slack to Discord
 * We parse the message sent from Zapier here and delete it
 * And finally we repost the message all in here.
 *
 * Disgusting right? Sorry, I don't know how to do it any other way. I hope a future dev
 * who knows a bit more about web development can fix this. But for now, it is what it is!
 * And it works (for the most part I guess..)!
 */
public class GuildMessageStash extends ListenerAdapter {

    private final String STASH_ICON = "https://img.favpng.com/5/6/10/bitbucket-portable-network-graphics-logo-github-repository-png-favpng-C52i9LPss9RJt5zsvs6EXNfpW.jpg";
    private final String JENKINS_ICON = "https://dyltqmyl993wv.cloudfront.net/assets/stacks/jenkins/img/jenkins-stack-220x234.png";

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {

        if (event.isWebhookMessage() && event.getAuthor().getName().equals("QMNs)dvE1x02")) {
            JSONObject jo = new JSONObject(event.getMessage().getContentRaw().replaceAll("\n", ""));
            event.getMessage().delete().queue();

            String sender = jo.getString("username");

            if ((sender.equals("Stash") || sender.equals("Jenkins"))) {
                WebhookUtils.retrieveWebhookUrl(event.getChannel()).thenAccept(url -> {
                    String message = jo.getString("message");

                    WebhookEmbedBuilder web = new WebhookEmbedBuilder();
                    String icon;

                    if (sender.equals("Jenkins")) {
                        icon = JENKINS_ICON;

                        Pattern pattern = Pattern.compile("(?<repo>.{1,32}) - (?<info1>#[0-9]{1,5}) (?<info2>.+) \\(<(?<url>http://jenkins\\.grandtheftmc\\.net/.+)\\|Open>\\)(?<extra>.*)");
                        Matcher matcher = pattern.matcher(message);
                        if (matcher.find()) {
                            String repo = matcher.group("repo");
                            String info1 = matcher.group("info1");
                            String info2 = matcher.group("info2");
                            String changeUrl = matcher.group("url").replace("http", "https");
                            String extra = null;
                            if (matcher.groupCount() == 5) {
                                extra = matcher.group("extra");
                            }

                            web.setTitle(new WebhookEmbed.EmbedTitle(repo + " [Open]", changeUrl));
                            web.setDescription("__" + info1 + "__" + " " + info2 + (extra == null ? "" : "\n" + extra));
                            web.setColor(Color.RED.getRGB());
                        }

                    } else {
                        icon = STASH_ICON;

                        //Push on https://stash.grandtheftmc.net/projects/GRAN/repos/discordbot/browse`GRAN/DiscordBot`> branch https://stash.grandtheftmc.net/projects/GRAN/repos/discordbot/commits?until=refs/heads/skylix_qa`skylix_qa`> by `Aseef Imran &lt;aseef.imran@grandtheftmc.net&gt;` (1 commit). See https://stash.grandtheftmc.net/projects/GRAN/repos/discordbot/commits?until=refs/heads/skylix_qacommit list>.
                        Pattern pattern = Pattern.compile("Push on (?<projectUrl>https://stash\\.grandtheftmc\\.net/projects/.+)`(?<project>GRAN/.{1,32})`> branch (?<commitUrl1>https://stash.grandtheftmc.net/projects/.+)`(?<branch>.{1,32})`> by `(?<user>.{1,48} &lt;.+@.+)&gt;` \\((?<count>[0-9]{1,4}) commits?\\). See (?<commitUrl2>https://stash.grandtheftmc.net/projects/.+)commit list>\\.");
                        Matcher matcher = pattern.matcher(message);

                        if (matcher.find()) {
                            String projectUrl = matcher.group("projectUrl");
                            String project = matcher.group("project");
                            String commitUrl = matcher.group("commitUrl1");
                            String branch = matcher.group("branch");
                            String user = matcher.group("user").replace("&lt;", "- ");
                            String count = matcher.group("count");

                            web.setTitle(new WebhookEmbed.EmbedTitle("**Code Pushed on Stash**", null));
                            web.setDescription("Received (" + count + ") incoming commit(s)!");
                            web.setColor(Color.CYAN.getRGB());

                            web.addField(new WebhookEmbed.EmbedField(true,"Project", project));
                            web.addField(new WebhookEmbed.EmbedField(true,"Branch", branch));
                            web.addField(new WebhookEmbed.EmbedField(false,"User", user));
                            web.addField(new WebhookEmbed.EmbedField(false,"Project URL", projectUrl));
                            web.addField(new WebhookEmbed.EmbedField(false,"View Commits", commitUrl));
                        }

                        //opened pull request <https://stash.grandtheftmc.net/projects/GRAN/repos/discordbot/pull-requests/10/overview|#10: Skylix qa>
                        Pattern pattern1 = Pattern.compile("(?<action>.{1,24}) pull request <(?<prUrl>https://stash\\.grandtheftmc\\.net/projects/.+)\\|#(?<prNumber>[0-9]{1,5}): (?<title>.+)>");
                        Matcher matcher1 = pattern1.matcher(message);
                        if (matcher1.find()) {

                            String action = matcher1.group("action");
                            String prUrl = matcher1.group("prUrl");
                            String prNumber = matcher1.group("prNumber");
                            String title = matcher1.group("title");

                            //https://stash.grandtheftmc.net/projects/GRAN/repos/discordbot/pull-requests/10/overview
                            Pattern urlPattern = Pattern.compile("https://stash\\.grandtheftmc\\.net/projects/GRAN/repos/(?<project>.{1,36})/pull-requests/.+");
                            Matcher m = urlPattern.matcher(prUrl);

                            if (m.find()) {
                                String project = m.group("project");

                                web.setTitle(new WebhookEmbed.EmbedTitle("**Pull request #" + prNumber + " was " + action + " on Stash**", null));
                                web.setColor(Color.GREEN.getRGB());

                                web.addField(new WebhookEmbed.EmbedField(true, "Project", project.toUpperCase()));
                                web.addField(new WebhookEmbed.EmbedField(true, "Title", title));
                                web.addField(new WebhookEmbed.EmbedField(false, "Pull Request URL", prUrl));
                            }
                        }

                    }

                    WebhookUtils.sendMessage(sender, icon, " ", web.build(), url);
                });
            }
        }

    }
}
