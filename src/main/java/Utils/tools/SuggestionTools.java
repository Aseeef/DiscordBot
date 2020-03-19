package Utils.tools;

import Utils.Config;
import Utils.Suggestions;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.awt.*;
import java.io.IOException;

import static Utils.tools.GTools.userById;

public class SuggestionTools {

    public static void setSuggestionsChannel (TextChannel channel) throws IOException {
        Config.get().setSuggestionChannelId(channel.getIdLong());
    }

    public static TextChannel getSuggestionsChannel (GuildMessageReceivedEvent e) {
        return e.getGuild().getTextChannelById(Config.get().getSuggestionChannelId());
    }

    public static MessageEmbed createSuggestionEmbed (Suggestions s, GuildMessageReceivedEvent e) {
        // Create suggestion embed
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("<:gtmlearnables:372027028796473344> SUGGESTION ID: #" + s.getNumber());
        embed.setThumbnail(userById(s.getSuggesterId()).getAvatarUrl());
        embed.setDescription(s.getMsg());
        embed.addField("Suggestion Status: ", "**"+s.getStatus()+"**: "+s.getStatusReason(), true);
        embed.setFooter("Submitted by " + userById(s.getSuggesterId()).getAsTag() +
                " (" + s.getSuggesterId() + ")");
        // Set embed color based on status
        if (s.getStatus().equals("PENDING")) embed.setColor(Color.ORANGE);
        else if (s.getStatus().equals("APPROVED")) embed.setColor(Color.GREEN);
        else if (s.getStatus().equals("DENIED")) embed.setColor(Color.RED);

        return embed.build();
    }

    public static MessageEmbed createHowSuggestionEmbed (GuildMessageReceivedEvent s) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("How to Make a Suggestion");
        embed.setColor(Color.GRAY);
        embed.setDescription("Welcome to the GTM Suggestions channel! To post your suggestion, simply **Copy and Paste** the below format in to chat. " +
                "If you do not copy and paste the suggestion format in to your suggestion, the **bot will delete your message**! " +
                "Please note, **once sent, you can NOT edit your suggestion**, so please proof read your message before sending.");
        return embed.build();
    }

    public static Message suggestionMessage() {
        MessageBuilder msg = new MessageBuilder()
                .append("```")
                .append("**What Server is your Suggestion for?**")
                .append("\n")
                .append("My suggestion is for...")
                .append("\n\u200E\n")
                .append("**What is your Suggestion? Be concise!**")
                .append("\n")
                .append("I want to suggest that...")
                .append("\n\u200E\n")
                .append("**Why do you Suggestion this?**")
                .append("\n")
                .append("I am suggesting this because...")
                .append("```");
        return msg.build();
    }

}
