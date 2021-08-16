package commands.suggestions;

import utils.BotData;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import utils.selfdata.ChannelIdData;
import utils.Utils;

import java.awt.*;
import java.util.concurrent.TimeUnit;

import static utils.Utils.*;

public class SuggestionTools {

    public static TextChannel getSuggestionsChannel () {
        return guild.getTextChannelById(ChannelIdData.get().getSuggestionChannelId());
    }

    public static MessageEmbed createSuggestionEmbed (Suggestions s) {
        // Create suggestion embed
        EmbedBuilder embed = new EmbedBuilder();
        String gtmLearnablesEmoji = JDA.getEmotesByName("gtmlearnables", true).get(0).getAsMention();
        embed.setTitle(gtmLearnablesEmoji + "  SUGGESTION ID: #" + s.getNumber());
        embed.setThumbnail(userById(s.getSuggesterId()).getAvatarUrl());
        embed.setDescription("\n\u200E"+s.getMsg()+"\n\u200E");
        embed.addField("**Suggestion Status:**", "**"+s.getStatus()+"**: "+s.getStatusReason(), true);
        embed.setFooter("Submitted by " + userById(s.getSuggesterId()).getAsTag() +
                " (" + s.getSuggesterId() + ")");
        // Set embed color based on status
        if (s.getStatus().equals("PENDING")) embed.setColor(Color.ORANGE);
        else if (s.getStatus().equals("APPROVED")) embed.setColor(Color.GREEN);
        else if (s.getStatus().equals("DENIED")) embed.setColor(Color.RED);
        else if (s.getStatus().equals("COMPLETE")) embed.setColor(Color.CYAN);

        return embed.build();
    }

    public static MessageEmbed createHowSuggestionEmbed () {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("How to Make a Suggestion");
        embed.setColor(Color.GRAY);
        embed.setDescription("Welcome to the GTM Suggestions channel! To post your suggestion, simply **Copy and Paste** the below format in to chat, " +
                "and then fill it out. If you do not copy and paste the suggestion format in to your suggestion, the **bot will delete your message**! " +
                "Please note, **once sent, you can NOT edit your suggestion**, so please proof read your message before sending.");
        return embed.build();
    }

    public static Message suggestionMessage() {
        MessageBuilder msg = new MessageBuilder()
                .append("```")
                .append("What Server is your Suggestion for?")
                .append("\n")
                .append("[Type what server here]")
                .append("\n\u200E\n")
                .append("What is your Suggestion? Be concise!")
                .append("\n")
                .append("[Explain suggestion here (10+ words)]")
                .append("\n\u200E\n")
                .append("Why do you Suggestion this?")
                .append("\n")
                .append("[Explain reason here (16+ words)]")
                .append("```");
        return msg.build();
    }

    // Send how to make a suggestion instructions for the next person & delete previous using callbacks
    public static void suggestionInstruct(TextChannel channel) {

        channel.sendMessage(createHowSuggestionEmbed()).queueAfter(5, TimeUnit.SECONDS, (embedMsg) -> {
            channel.sendMessage(suggestionMessage()).queueAfter(250, TimeUnit.MILLISECONDS, (rawMsg) -> {

                // These longs will always store the id of the previous suggestion instruction msgs
                long prevSuggestHelpEmbedId = BotData.LAST_SUGGEST_EMBED_ID.getData(Long.TYPE);
                long prevSuggestHelpMsgId = BotData.LAST_SUGGEST_MSG_ID.getData(Long.TYPE);
                TextChannel prevSuggestHelpChannel = JDA.getTextChannelById(ChannelIdData.get().getPrevSuggestHelpChannelId());

                // Delete previous instruction embed & msg
                if (prevSuggestHelpChannel != null) {
                    prevSuggestHelpChannel.retrieveMessageById(prevSuggestHelpEmbedId).queue( (prevEmbed) -> {
                        if (prevEmbed != null)
                            prevEmbed.delete().queue();
                            });
                    prevSuggestHelpChannel.retrieveMessageById(prevSuggestHelpMsgId).queue( (prevMsg) -> {
                        if (prevMsg != null)
                            prevMsg.delete().queue();
                    });
                }

                // Save current embed & msg ids
                BotData.LAST_SUGGEST_EMBED_ID.setValue(embedMsg.getIdLong());
                BotData.LAST_SUGGEST_MSG_ID.setValue(rawMsg.getIdLong());
                ChannelIdData.get().setPrevSuggestHelpChannelId(channel.getIdLong());

            });

        });

    }

    public static String formatSuggestion(String msg) {
        if (msg.startsWith("```")) msg = msg.replaceFirst("```", "");
        if (msg.endsWith("```")) msg = Utils.replaceLast(msg, "```", "");
        msg = msg.replaceFirst(".*What Server is your Suggestion for\\?.*\n", "**What Server is your Suggestion for?**\n");
        msg = msg.replaceFirst(".*What is your Suggestion\\? Be concise!.*\n", "**What is your Suggestion? Be concise!**\n");
        msg = msg.replaceFirst(".*Why do you Suggestion this\\?.*\n", "**Why do you Suggestion this?**\n");
        return msg;
    }

}
