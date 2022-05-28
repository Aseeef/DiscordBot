package net.grandtheftmc.discordbot.commands.suggestions;

import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.grandtheftmc.discordbot.GTMBot;
import net.grandtheftmc.discordbot.utils.Data;
import net.grandtheftmc.discordbot.utils.Utils;
import net.grandtheftmc.discordbot.utils.console.Logs;
import net.grandtheftmc.discordbot.utils.users.Rank;

import java.util.Objects;

public class SuggestionListener extends ListenerAdapter {

    public void onMessageReceived(MessageReceivedEvent e) {

        if (!e.isFromGuild()) return;

        TextChannel channel = e.getTextChannel();
        User user = e.getAuthor();


        // If it is a suggestion and not a command
        if (SuggestionTools.getSuggestionsChannel() == channel
                && !user.isBot()
                && !Utils.isCommand(e.getMessage().getContentRaw(), user)) {

            // Delete original message
            e.getMessage().delete().queue();

            String message = SuggestionTools.formatSuggestion(e.getMessage().getContentRaw());

            // If not verified
            if (!Rank.hasRolePerms(e.getMember(), Rank.NORANK)) {

                Utils.sendThenDelete(channel, user.getAsMention() + " you are not allowed to post suggestions " +
                        "until you have verified your discord account to GTM by using the `/discord verify` command in game!");

                // DM user their deleted suggestion so it can be reposted
                user.openPrivateChannel().queue(
                        userChannel ->
                                userChannel.sendMessage("**Your suggestion was deleted because you are not a verified user:**\n```" + e.getMessage().getContentRaw() + "```\n" + "**Please link your account to GTM using the `/discord verify` command in game and repost this suggestion.**")
                                        .queue());

                // Log failure
                Logs.log("Non Verified User: Deleted the following suggestion from user " + user.getAsTag() + " (" + user.getId() + "):" +
                        "\n" + e.getMessage().getContentRaw());

                return;
            }

            // If user didn't use suggestion format, delete their msg
            if (!ifUsedFormat(message)) {

                Utils.sendThenDelete(channel, user.getAsMention() + " your message does not follow the suggestion format! " +
                        "Please follow the above given instructions and repost your suggestion.");

                // DM user their deleted suggestion so it can be reposted
                user.openPrivateChannel().queue(

                        userChannel ->
                    userChannel.sendMessage("**Your suggestion was deleted because it did not follow the suggestion format:**\n```" + e.getMessage().getContentRaw() + "```\n" + "**Please copy paste this exact format in to your message and repost your suggestion:**\n")
                            .queue( (success) ->
                            userChannel.sendMessage(SuggestionTools.suggestionMessage()).queue()));

                // Log failure
                Logs.log("Incorrect Format: Deleted the following suggestion from user "+user.getAsTag()+" ("+user.getId()+"):"+
                        "\n"+e.getMessage().getContentRaw());

                return;
            }

            // Create new suggestion object
            Suggestions suggestion = new Suggestions(Data.getNextNumber(Data.SUGGESTIONS), message, Objects.requireNonNull(e.getMember()).getId(), "PENDING", "Awaiting response from staff.");

            // Create and send suggestion embed then use the callback to save msg id into suggestion object & add reaction
                e.getChannel().sendMessageEmbeds(SuggestionTools.createSuggestionEmbed(suggestion)).queue( (msg) -> {
                    // Set suggestion id
                    suggestion.setId(msg.getIdLong());
                    // React to the message
                    Emote gtmAgree = GTMBot.getJDA().getEmotesByName("gtmagree", true).get(0);
                    Emote gtmDisagree = GTMBot.getJDA().getEmotesByName("gtmdisagree", true).get(0);
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
            Logs.log("User "+user.getAsTag()+
                    "("+user.getIdLong()+") just created a suggestion with ID #"+
                    suggestion.getNumber());

            // Send how to make a suggestion instructions for the next person
            SuggestionTools.suggestionInstruct(channel);

        }

    }

    private boolean ifUsedFormat(String msg) {
        return msg.toLowerCase().contains("**what server is your suggestion for?**") &&
                msg.toLowerCase().contains("**what is your suggestion? be concise!**") &&
                msg.toLowerCase().contains("**why do you suggestion this?**");
    }

}
