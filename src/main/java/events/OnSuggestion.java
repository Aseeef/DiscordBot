package events;

import Utils.AutoDeleter.DeleteMe;
import Utils.Data;
import Utils.Suggestions;
import Utils.tools.GTools;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static Utils.tools.GTools.*;
import static Utils.tools.SuggestionTools.*;

public class OnSuggestion extends ListenerAdapter {

    // This long will always store the id of the previous suggestion instruction msgs
    private static long suggestionHelpEmbedId = 0;
    private static long suggestionHelpMessageId = 0;

    public void onGuildMessageReceived(GuildMessageReceivedEvent e) {

        TextChannel channel = e.getChannel();

        // If it is a suggestion and not a command
        if (getSuggestionsChannel(e) == channel
                && !e.getAuthor().isBot()
                && !GTools.isCommand(e)) {

            // Delete original message
            e.getMessage().delete().queue();

            if (!e.getMessage().getContentRaw().toLowerCase().contains("what server is your suggestion for") ||
                    !e.getMessage().getContentRaw().toLowerCase().contains("what is your suggestion") ||
                    !e.getMessage().getContentRaw().toLowerCase().contains("why do you suggestion this")) {

                channel.sendMessage(e.getAuthor().getAsMention() + " your message does not follow the suggestion format!").queue();
                DeleteMe.deleteQueue(e.getAuthor().getAsMention() + " your message does not follow the suggestion format!");

                // Log failure
                log("Incorrect Format: Deleted the following suggestion from user "+e.getAuthor().getAsTag()+" ("+e.getAuthor().getId()+"):"+
                        "\n"+e.getMessage().getContentRaw());

                return;

            }

            // Create new suggestion object
            Suggestions suggestion = new Suggestions(Data.getNextNumber(Data.SUGGESTIONS), e.getMessage().getContentRaw(), Objects.requireNonNull(e.getMember()).getId(), "PENDING", "Awaiting response from staff.");

            // Create and send suggestion embed
                e.getChannel().sendMessage(
                        createSuggestionEmbed(suggestion, e)
                ).queue();

            // Send how to make a suggestion instructions
            channel.sendMessage(createHowSuggestionEmbed(e)).queueAfter(900, TimeUnit.MILLISECONDS);
            channel.sendMessage(suggestionMessage()).queueAfter(1000, TimeUnit.MILLISECONDS);

        }

        // If the message is the suggestion reformatted and sent by the bot, add reactions to it
        if (getSuggestionsChannel(e) == channel
                && e.getAuthor().isBot()
                && e.getMessage().getEmbeds().size() != 0
                && e.getMessage().getEmbeds().get(0).getTitle().contains("SUGGESTION")
        ) {

            // React to the message
            Emote gtmAgree = e.getGuild().getEmoteById("372027012501471242");
            Emote gtmDisagree = e.getGuild().getEmoteById("372027027399901194");
            assert gtmAgree != null;
            assert gtmDisagree != null;
            channel.addReactionById(e.getMessageId(), gtmAgree).queue();
            channel.addReactionById(e.getMessageId(), gtmDisagree).queue();

            // Get the suggestion that was just sent
            Suggestions suggestion = null;
            try {
                suggestion = Data.obtainData(Data.SUGGESTIONS, Data.getCurrentNumber(Data.SUGGESTIONS));
            } catch (IOException | ClassNotFoundException ex) {
                ex.printStackTrace();
            }
            // Set suggestion message ID (right now it was null)
            try {
                suggestion.setId(e.getMessageIdLong());
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            // Notify user a success message & inform them that they will get alerts on status change
            userById(suggestion.getSuggesterId()).openPrivateChannel().queue((userChannel) ->
            {
                // Send a private message to the user
                userChannel.sendMessage("**Hello! Thank you for your suggestion! " +
                        "I will notify you if there are any updates on the status of your suggestion. Have a great day!**").queue();
            });

            // Log suggestion
            log("User "+GTools.userById(suggestion.getSuggesterId()).getAsTag()+
                    "("+suggestion.getSuggesterId()+") just created a suggestion with ID #"+
                    suggestion.getNumber());

        }

        // Delete previous suggestion help and format msg
        if (getSuggestionsChannel(e) == e.getChannel()
                && e.getAuthor().isBot()
                && e.getMessage().getEmbeds().size() != 0
                && e.getMessage().getEmbeds().get(0).getTitle().equals("How to Make a Suggestion")) {
            // Delete previous embed
            if (suggestionHelpEmbedId != 0) {
                channel.deleteMessageById(suggestionHelpEmbedId).queue();
            }
            // Save current embed id
            suggestionHelpEmbedId = e.getMessageIdLong();
        } else if (getSuggestionsChannel(e) == e.getChannel()
                && e.getAuthor().isBot()
                && e.getMessage().getContentRaw().equals(suggestionMessage().getContentRaw())) {
            // Delete previous embed
            if (suggestionHelpMessageId != 0) {
                channel.deleteMessageById(suggestionHelpMessageId).queue();
            }
            // Save current embed id
            suggestionHelpMessageId = e.getMessageIdLong();
        }

    }

}
