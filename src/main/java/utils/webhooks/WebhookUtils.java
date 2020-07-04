package utils.webhooks;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.Webhook;

import java.util.concurrent.CompletableFuture;

import static utils.tools.GTools.jda;

public class WebhookUtils {

    /**
     * Retrieve a web hook from either an existing web hook but if none are found create a new web hook for the specified channel
     * @param channel - The channel for which to get the web hook for.
     * @return - Returns a CompletableFuture url String which can / should be completed async in lamda using CompleteableFuture#thenCompleteAsync();
     */
    public static CompletableFuture<String> retrieveWebhookUrl (TextChannel channel) {
        CompletableFuture<String> futureHookUrl = new CompletableFuture<>();
        jda.getGuilds().get(0).retrieveWebhooks().queue( (webhooks -> {
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
        try (WebhookClient client = WebhookUtils.getWebhookClient(hookUrl)) {
            // Change appearance of webhook message to match target
            WebhookMessageBuilder mb = new WebhookMessageBuilder();
            mb.setUsername(target.getEffectiveName()); // use this username
            mb.setAvatarUrl(target.getUser().getAvatarUrl()); // use this avatar
            mb.setContent(message);
            client.send(mb.build());
        }
    }

}
