package net.grandtheftmc.discordbot.utils;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.Webhook;
import net.grandtheftmc.discordbot.GTMBot;

import javax.annotation.Nullable;
import java.io.File;
import java.util.concurrent.CompletableFuture;

public class WebhookUtils {

    /**
     * Retrieve a web hook from either an existing web hook but if none are found create a new web hook for the specified channel
     * @param channel - The channel for which to get the web hook for.
     * @return - Returns a CompletableFuture url String which can / should be completed async in lamda using CompleteableFuture#thenCompleteAsync();
     */
    public static CompletableFuture<String> retrieveWebhookUrl (TextChannel channel) {
        CompletableFuture<String> futureHookUrl = new CompletableFuture<>();
        GTMBot.getJDA().getGuilds().get(0).retrieveWebhooks().queue( (webhooks -> {
            Webhook hook = webhooks.stream().filter((webhook -> webhook.getChannel().getIdLong() == channel.getIdLong())).findFirst().orElse(null);
            if (hook != null)
                futureHookUrl.complete(hook.getUrl());
            else channel.createWebhook(channel.getAsMention() + " (GTM Bot)").queue( (hook2) ->
                    futureHookUrl.complete(hook2.getUrl()));
        }));
        return futureHookUrl;
    }

    public static WebhookClient getWebhookClient (String hookUrl) {
        WebhookClientBuilder builder = new WebhookClientBuilder(hookUrl);
        builder.setThreadFactory((job) -> {
            Thread thread = new Thread(job);
            thread.setName("Webhook-Thread");
            thread.setDaemon(true);
            return thread;
        });
        builder.setWait(true);
        return builder.build();
    }

    public static void sendMessageAs (String message, Member target, String hookUrl) {
        sendMessageAs(message, target, hookUrl, null);
    }

    public static void sendMessageAs (String message, Member target, String hookUrl, @Nullable File file) {
        sendMessage(target.getEffectiveName(), target.getUser().getAvatarUrl(), message, null, hookUrl, file);
    }

    public static void sendMessage (String name, String iconUrl, String message, String hookUrl) {
        sendMessage(name, iconUrl, message, null, hookUrl, null);
    }

    public static void sendMessage (String name, String iconUrl, String message, WebhookEmbed embed, String hookUrl) {
        sendMessage(name, iconUrl, message, embed, hookUrl, null);
    }

    public static void sendMessage (String name, String iconUrl, String message, @Nullable WebhookEmbed embed, String hookUrl, @Nullable File file) {

        try (WebhookClient client = WebhookUtils.getWebhookClient(hookUrl)) {
            // Change appearance of webhook message to match target
            WebhookMessageBuilder mb = new WebhookMessageBuilder();
            mb.setUsername(name); // use this username
            mb.setAvatarUrl(iconUrl); // use this avatar
            mb.setContent(message);
            if (embed != null) {
                mb.addEmbeds(embed);
            }
            if (file != null)
                mb.addFile(file);
            client.send(mb.build());
        }
    }

}
