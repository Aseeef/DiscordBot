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

    public static CompletableFuture<String> retrieveWebhookUrl (TextChannel channel) {
        CompletableFuture<String> futureHookUrl = new CompletableFuture<>();
        jda.getGuilds().get(0).retrieveWebhooks().queue( (webhooks -> {
            Webhook hook = webhooks.stream().filter((webhook -> webhook.getChannel().getIdLong() == channel.getIdLong())).findFirst().orElse(null);
            if (hook != null)
                futureHookUrl.complete(hook.getUrl());
            else futureHookUrl.complete(null);
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
