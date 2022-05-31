package net.grandtheftmc.discordbot.events;

import net.grandtheftmc.discordbot.utils.Utils;
import net.grandtheftmc.discordbot.utils.selfdata.AnnoyData;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.grandtheftmc.discordbot.utils.WebhookUtils;

import java.util.List;
import java.util.Map;

public class OnGuildMessage extends ListenerAdapter {

    public void onMessageReceived (MessageReceivedEvent e) {
        User user = e.getAuthor();
        Member member = e.getMember();
        final Message message = e.getMessage();

        if (!e.isFromGuild() || member == null || user.isBot() || message.isWebhookMessage() || Utils.isCommand(message.getContentRaw(), user)) return;

        final Map<Long, String> annoyMap = AnnoyData.get().getEmojiAnnoyMap();
        final Map<Long, Character> scrabbleMap = AnnoyData.get().getScrabbleAnnoyMap();
        final List<Long> botMap = AnnoyData.get().getBotAnnoyList();

        if (annoyMap.containsKey(user.getIdLong())) {
            String emojiString = annoyMap.get(user.getIdLong());
            Emote emoji;
            try {
                emoji =  e.getGuild().getEmoteById(emojiString);
                if (emoji != null)
                    message.addReaction(emoji).queue(null, error -> {
                        annoyMap.remove(user.getIdLong());
                        AnnoyData.get().save();
                    });
            } catch (NumberFormatException er) {
                message.addReaction(emojiString).queue(null, error -> {
                    annoyMap.remove(user.getIdLong());
                    AnnoyData.get().save();
                });
            }

        }

        else if (scrabbleMap.containsKey(user.getIdLong())) {
            message.delete().queue();
            WebhookUtils.retrieveWebhookUrl(e.getTextChannel()).thenAccept((hookUrl) -> {
                //String[] words = message.getContentRaw().split(" ");

                String fullMsg = message.getContentRaw();
                fullMsg = fullMsg.replaceFirst("[a-zA-Z]", String.valueOf(scrabbleMap.get(user.getIdLong())));
                fullMsg = fullMsg.replaceAll("([^0-9a-zA-Z])[0-9a-zA-Z]", "$1" + scrabbleMap.get(user.getIdLong()));

                /*
                StringBuilder sb = new StringBuilder();
                for (int i = 0 ; i < words.length ; i++) {
                    String word = words[i];
                    word = word.replaceFirst("[a-zA-Z]", String.valueOf(scrabbleMap.get(user.getIdLong())));
                    if (i != 0) sb.append(" ");
                    sb.append(word);
                }
                 */

                if (hookUrl != null)
                    WebhookUtils.sendMessageAs(fullMsg, member, hookUrl);
            });
        }

        else if (botMap.contains(user.getIdLong())) {
            message.delete().queue();
            WebhookUtils.retrieveWebhookUrl(e.getTextChannel()).thenAccept((hookUrl) -> {
                if (hookUrl != null) {
                    WebhookUtils.sendMessageAs(e.getMessage().getContentRaw().replaceAll("@everyone", "everyone"), member, hookUrl);
                }
            });
        }

    }

}
