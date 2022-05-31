package net.grandtheftmc.discordbot.commands.suggestions;

import club.minnced.discord.webhook.WebhookClientBuilder;
import net.dv8tion.jda.api.entities.WebhookClient;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.grandtheftmc.discordbot.GTMBot;
import net.grandtheftmc.discordbot.utils.BotData;
import net.grandtheftmc.discordbot.utils.Utils;
import net.grandtheftmc.discordbot.utils.selfdata.ChannelIdData;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.*;
import java.util.concurrent.TimeUnit;

import static net.grandtheftmc.discordbot.utils.Utils.*;

public class SuggestionTools {

    public static TextChannel getSuggestionsChannel () {
        return GTMBot.getGTMGuild().getTextChannelById(ChannelIdData.get().getSuggestionChannelId());
    }

    public static MessageEmbed createSuggestionEmbed (Suggestions s) {
        // Create suggestion embed
        EmbedBuilder embed = new EmbedBuilder();
        String gtmLearnablesEmoji = GTMBot.getJDA().getEmotesByName("gtmlearnables", true).get(0).getAsMention();
        embed.setTitle(gtmLearnablesEmoji + "  SUGGESTION ID: #" + s.getNumber());
        embed.setThumbnail(userById(s.getSuggesterId()).getAvatarUrl());
        embed.addField("**What is this suggest for?**", s.getServer(), false);
        embed.addField("**What is your Suggestion? Be concise and clear!**", s.getSuggestion(), false);
        embed.addField("**Why should we implement this suggestion?**", s.getSuggestion(), false);
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

    public static MessageEmbed createHowSuggestionEmbed() {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("✏️ How to Make a Suggestion");
        embed.setColor(Color.GRAY);
        embed.setDescription("Welcome to the GTM Suggestions channel! To post your suggestion, use the **click** the button below!\n\n" +
                "After clicking the below button, you will be prompted with a forum which you will then fill out. To prevent spam, you " +
                "**must be verified with `/discord verify`** in order to use suggestions! Finally, note, " +
                "**once sent, you can NOT edit your suggestion**, so please proof read your message before sending.");
        return embed.build();
    }

    // Send how to make a suggestion instructions for the next person & delete previous using callbacks
    public static void suggestionInstruct(TextChannel channel) {

        channel.sendMessageEmbeds(createHowSuggestionEmbed())
                .setActionRow(Button.primary("suggest-btn", "Create Suggestion!"))
                .queueAfter(5, TimeUnit.SECONDS, (embedMsg) -> {

            // These longs will always store the id of the previous suggestion instruction msgs
            long prevSuggestHelpEmbedId = BotData.LAST_SUGGEST_EMBED_ID.getData(Long.TYPE);
            TextChannel prevSuggestHelpChannel = GTMBot.getJDA().getTextChannelById(ChannelIdData.get().getPrevSuggestHelpChannelId());

            // Delete previous instruction embed & msg
            if (prevSuggestHelpChannel != null) {
                prevSuggestHelpChannel.retrieveMessageById(prevSuggestHelpEmbedId).queue((prevEmbed) -> {
                    if (prevEmbed != null)
                        prevEmbed.delete().queue();
                });
            }

            // Save current embed & msg ids
            BotData.LAST_SUGGEST_EMBED_ID.setValue(embedMsg.getIdLong());
            ChannelIdData.get().setPrevSuggestHelpChannelId(channel.getIdLong());

        });

    }

    public static Modal getSuggestModal() {
        return Modal
                .create("suggest-create", "Create a New Suggestion")
                .addActionRow(
                        TextInput.create(
                                        "server",
                                        "What is this suggest for?",
                                        TextInputStyle.SHORT
                                ).setMinLength(3)
                                .setRequired(true)
                                .setPlaceholder("(e.i. for discord, for gtm1, for all gtms...?)")
                                .build()
                )
                .addActionRow(
                        TextInput.create(
                                        "suggestion",
                                        "What is your Suggestion? Be concise!",
                                        TextInputStyle.PARAGRAPH
                                ).setMinLength(12)
                                .setRequired(true)
                                .setPlaceholder("My suggestion is that we...")
                                .build()
                )
                .addActionRow(
                        TextInput.create(
                                        "reason",
                                        "Why should we implement this suggestion?",
                                        TextInputStyle.PARAGRAPH
                                ).setMinLength(18)
                                .setRequired(true)
                                .setPlaceholder("You should add this suggestion because...")
                                .build()
                )
                .build();
    }

}
