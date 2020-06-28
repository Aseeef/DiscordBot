package events;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import utils.SelfData;
import utils.tools.GTools;
import utils.webhooks.WebhookUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OnGuildMessage extends ListenerAdapter {

    public void onGuildMessageReceived (GuildMessageReceivedEvent e) {
        User user = e.getAuthor();
        Member member = e.getMember();
        final Message message = e.getMessage();

        if (member == null || user.isBot() || message.isWebhookMessage() || GTools.isCommand(message.getContentRaw(), user)) return;

        final Map<Long, String> annoyMap = SelfData.get().getEmojiAnnoyMap();
        final Map<Long, Character> scrabbleMap = SelfData.get().getScrabbleAnnoyMap();
        final List<Long> botMap = SelfData.get().getBotAnnoyList();

        if (annoyMap.containsKey(user.getIdLong())) {
            String emoji = annoyMap.get(user.getIdLong());
            message.addReaction(emoji).queue(null, error -> {
                annoyMap.remove(user.getIdLong());
                SelfData.get().update();
            });
        }

        else if (scrabbleMap.containsKey(user.getIdLong())) {
            message.delete().queue();
            WebhookUtils.retrieveWebhookUrl(e.getChannel()).thenAcceptAsync((hookUrl) -> {
                String[] words = message.getContentRaw().split(" ");
                StringBuilder sb = new StringBuilder();
                for (int i = 0 ; i < words.length ; i++) {
                    String word = words[i];
                    word = word.replaceFirst("[a-zA-Z]", String.valueOf(scrabbleMap.get(user.getIdLong())));
                    if (i != 0) sb.append(" ");
                    sb.append(word);
                }
                if (hookUrl != null)
                    WebhookUtils.sendMessageAs(sb.toString(), member, hookUrl);
            });
        }

        else if (botMap.contains(user.getIdLong())) {
            message.delete().queue();
            WebhookUtils.retrieveWebhookUrl(e.getChannel()).thenAcceptAsync((hookUrl) -> {
                if (hookUrl != null)
                    WebhookUtils.sendMessageAs(e.getMessage().getContentRaw(), member, hookUrl);
            });
        }

    }

}
