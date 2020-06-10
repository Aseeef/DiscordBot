package events;

import Utils.Data;
import Utils.Suggestions;
import Utils.tools.GTools;
import Utils.tools.SuggestionTools;
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.Objects;

import static Utils.tools.GTools.jda;
import static Utils.tools.GTools.sendThenDelete;
import static Utils.console.Logs.log;
import static Utils.tools.SuggestionTools.*;

public class OnSuggestion extends ListenerAdapter {

    public void onGuildMessageReceived(GuildMessageReceivedEvent e) {

        TextChannel channel = e.getChannel();
        User user = e.getAuthor();

        // If it is a suggestion and not a command
        if (getSuggestionsChannel() == channel
                && !user.isBot()
                && !GTools.isCommand(e.getMessage().getContentRaw(), e.getAuthor())) {

            // Delete original message
            e.getMessage().delete().queue();

            // If user didn't use suggestion format, delete their msg
            if (!ifUsedFormat(e.getMessage().getContentRaw())) {

                sendThenDelete(channel, user.getAsMention() + " your message does not follow the suggestion format! " +
                        "Please follow the above given instructions and repost your suggestion.");

                // DM user their deleted suggestion so it can be reposted
                user.openPrivateChannel().queue((userChannel) ->
                    userChannel.sendMessage("**Your suggestion was deleted because it did not follow the suggestion format:**\n```" + e.getMessage().getContentRaw() + "```\n" + "**Please copy paste this exact format in to your message and repost your suggestion:**\n")
                            .queue( (success) ->
                            userChannel.sendMessage(suggestionMessage()).queue())
                        );

                // Log failure
                log("Incorrect Format: Deleted the following suggestion from user "+user.getAsTag()+" ("+user.getId()+"):"+
                        "\n"+e.getMessage().getContentRaw());

                return;

            }

            // Create new suggestion object
            Suggestions suggestion = new Suggestions(Data.getNextNumber(Data.SUGGESTIONS), e.getMessage().getContentRaw(), Objects.requireNonNull(e.getMember()).getId(), "PENDING", "Awaiting response from staff.");

            // Create and send suggestion embed then use the callback to save msg id into suggestion object & add reaction
                e.getChannel().sendMessage(createSuggestionEmbed(suggestion)).queue( (msg) -> {
                    // Set suggestion id
                    suggestion.setId(msg.getIdLong());
                    // React to the message
                    Emote gtmAgree = jda.getEmotesByName("gtmagree", true).get(0);
                    Emote gtmDisagree = jda.getEmotesByName("gtmdisagree", true).get(0);
                    msg.addReaction(gtmAgree).queue();
                    msg.addReaction(gtmDisagree).queue();
                });

            // Notify user a success message & inform them that they will get alerts on status change
            user.openPrivateChannel().queue((userChannel) ->
            {
                // Send a private message to the user
                userChannel.sendMessage("**Hello! Thank you for your suggestion! " +
                        "I will notify you if there are any updates on the status of your suggestion. Have a great day!**").queue();
            });

            // Log suggestion
            log("User "+user.getAsTag()+
                    "("+user.getIdLong()+") just created a suggestion with ID #"+
                    suggestion.getNumber());

            // Send how to make a suggestion instructions for the next person
            SuggestionTools.suggestionInstruct(channel);

        }

    }

    private boolean ifUsedFormat(String msg) {
        return msg.toLowerCase().contains("what server is your suggestion for") &&
                msg.toLowerCase().contains("what is your suggestion") &&
                msg.toLowerCase().contains("why do you suggestion this");
    }

}
