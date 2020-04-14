package commands;

import Utils.Data;
import Utils.Rank;
import Utils.SelfData;
import Utils.Suggestions;
import Utils.tools.GTools;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import static Utils.tools.GTools.*;
import static Utils.tools.SuggestionTools.*;

public class SuggestionCommand extends ListenerAdapter {


    public void onGuildMessageReceived(GuildMessageReceivedEvent e) {

        String msg = e.getMessage().getContentRaw();
        Member member = e.getMember();
        User user = e.getAuthor();
        assert member != null;

        if (GTools.isCommand(msg, user, Commands.SUGGESTION) &&
                hasRolePerms(member, Commands.SUGGESTION.rank())
        ) {

            String[] args = getArgs(msg);
            TextChannel channel = e.getChannel();

            // If there are no command arguments send sub command help list
            if (args.length == 0) {
                sendThenDelete(channel, getSuggestionsHelpMsg());
            }

            // Suggestions SetChannel Command
            else if (args[0].equalsIgnoreCase("setchannel")) {

                // Set suggestions settings
                SelfData.get().setSuggestionChannelId(channel.getIdLong());

                sendThenDelete(channel, suggestChannelSet(channel));

                // Send how to make a suggestion instruction
                suggestionInstruct(channel);

            }

            // Suggestions Deny Command
            else if (args[0].equalsIgnoreCase("deny")) {
                if (Data.doesNumberExist(Data.SUGGESTIONS, Integer.parseInt(args[1]))) {

                    // Set suggestion instance
                    Suggestions suggestion = (Suggestions) Data.obtainData(Data.SUGGESTIONS, Integer.parseInt(args[1]));

                    // Set it to denied
                    suggestion.setStatus("DENIED");
                    suggestion.setStatusReason(generateStatusReason(args));

                    // Build and edit denied suggestion embed
                    getSuggestionsChannel(e).editMessageById(suggestion.getId(), createSuggestionEmbed(suggestion)).queue();

                    final Suggestions pmSuggest = suggestion;

                    // Notify user about the results of their suggestion
                    userById(suggestion.getSuggesterId()).openPrivateChannel().queue((userChannel) ->
                    {
                        // Send a private message to the user
                        userChannel.sendMessage("**Hey there! Staff have updated the status of your suggestion:**").queue();
                        // Send suggestion embed
                        userChannel.sendMessage(createSuggestionEmbed(pmSuggest)).queue();
                    });

                    // Send message in channel informing executor that the command was ran
                    sendThenDelete(channel, "**Suggestion #"+suggestion.getNumber()+" has been set to DENIED!**");

                }
                // If none of the suggestions match provided id
                else {
                    sendThenDelete(channel, "**Suggestion not found. Please check your command!**");
                }
            }

            // Suggestions Approve Command
            else if (args[0].equalsIgnoreCase("approve")) {
                if (Data.doesNumberExist(Data.SUGGESTIONS, Integer.parseInt(args[1]))) {

                    Suggestions suggestion = (Suggestions) Data.obtainData(Data.SUGGESTIONS, Integer.parseInt(args[1]));
                    // Set new data
                    suggestion.setStatus("APPROVED");
                    suggestion.setStatusReason(generateStatusReason(args));
                    // Build and edit denied suggestion embed
                    getSuggestionsChannel(e).editMessageById(suggestion.getId(), createSuggestionEmbed(suggestion)).queue();

                    // Notify user about the results of their suggestion
                    userById(suggestion.getSuggesterId()).openPrivateChannel().queue((userChannel) ->
                    {
                        // Send a private message to the user
                        userChannel.sendMessage("**Hey there! Staff have updated the status of your suggestion:**").queue();
                        // Send suggestion embed
                        userChannel.sendMessage(createSuggestionEmbed(suggestion)).queue();
                    });

                    // Send message in channel informing executor that the command was ran
                    sendThenDelete(channel, "**Suggestion #"+suggestion.getNumber()+" has been set to APPROVED!**");

                }
                // If none of the suggestions match provided id
                else {
                    sendThenDelete(channel, "**Suggestion not found. Please check your command!**");
                }
            }

            // Suggestions Hold Command
            else if (args[0].equalsIgnoreCase("hold")) {
                if (Data.doesNumberExist(Data.SUGGESTIONS, Integer.parseInt(args[1]))) {

                    Suggestions suggestion = (Suggestions) Data.obtainData(Data.SUGGESTIONS, Integer.parseInt(args[1]));
                    // Set new data
                    suggestion.setStatus("PENDING");
                    suggestion.setStatusReason(generateStatusReason(args));
                    // Build and edit denied suggestion embed
                    getSuggestionsChannel(e).editMessageById(suggestion.getId(), createSuggestionEmbed(suggestion)).queue();

                    // Notify user about the results of their suggestion
                    userById(suggestion.getSuggesterId()).openPrivateChannel().queue((userChannel) ->
                    {
                        // Send a private message to the user
                        userChannel.sendMessage("**Hey there! Staff have updated the status of your suggestion:**").queue();
                        // Send suggestion embed
                        userChannel.sendMessage(createSuggestionEmbed(suggestion)).queue();
                    });

                    // Send message in channel informing executor that the command was ran
                    sendThenDelete(channel, "**Suggestion #"+suggestion.getNumber()+" has been set to PENDING!**");

                }
                // If none of the suggestions match provided id
                else {
                    sendThenDelete(channel, "**Suggestion not found. Please check your command!**");
                }
            }

            // If no sub commands match
            else {
                sendThenDelete(channel, getSuggestionsHelpMsg());
            }

        }

    }

    private static Message suggestChannelSet(TextChannel channel) {
        return new MessageBuilder().setContent(
                "**" + channel.getAsMention() + " has been set as the suggestions channel!**"
        ).build();
    }

    private static Message getSuggestionsHelpMsg() {
        return new MessageBuilder()
                .append("> **Please enter a valid command argument:**\n")
                .append("> `/Suggestion SetChannel` - *Set current channel to the suggestion channel*\n")
                .append("> `/Suggestion Approve <ID> (Reason)` - *Approve a suggestion*\n")
                .append("> `/Suggestion Deny <ID> (Reason)` - *Deny a suggestion*\n")
                .append("> `/Suggestion Hold <ID> (Reason)` - *Put a suggestion on hold*")
                .build();
    }

    private String generateStatusReason(String[] args) {
        if (args.length > 2) {
            StringBuilder string = new StringBuilder();
            for (int i = 2 ; i < args.length ; i++) {
                string.append(" ").append(args[i]);
            }
            return string.toString();
        }

        else
            return "No reason specified";
    }

}
