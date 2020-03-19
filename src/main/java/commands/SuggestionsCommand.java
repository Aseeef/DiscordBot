package commands;

import Utils.Data;
import Utils.AutoDeleter.DeleteMe;
import Utils.Suggestions;
import Utils.tools.GTools;
import Utils.tools.SuggestionTools;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static Utils.tools.GTools.userById;
import static Utils.tools.SuggestionTools.*;

public class SuggestionsCommand extends ListenerAdapter {

    public void onGuildMessageReceived(GuildMessageReceivedEvent e) {
        String[] args = e.getMessage().getContentRaw().split(" ");
        if (GTools.isCommand(e, "suggestion")
                && Objects.requireNonNull(e.getMember()).getPermissions().contains(Permission.ADMINISTRATOR)) {

            TextChannel channel = e.getChannel();

            // If there are no command arguments send sub command help list
            if (args.length == 1) {
                // Queue msg to be deleted
                DeleteMe.deleteQueue(getSuggestionsHelpMsg());
                // Send msg
                channel.sendMessage(getSuggestionsHelpMsg()).queue();
            }

            // Suggestions SetChannel Command
            else if (args[1].equalsIgnoreCase("setchannel")) {
                // Set suggestions settings
                try {
                    SuggestionTools.setSuggestionsChannel(channel);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

                // Queue msg to be deleted
                DeleteMe.deleteQueue(suggestChannelSet(channel));

                // Send channel to channel
                channel.sendMessage(suggestChannelSet(channel)).queue();

                // Send how to make a suggestion instructions
                channel.sendMessage(createHowSuggestionEmbed(e)).queueAfter(900, TimeUnit.MILLISECONDS);
                channel.sendMessage(suggestionMessage()).queueAfter(1000, TimeUnit.MILLISECONDS);
            }

            // Suggestions Deny Command
            else if (args[1].equalsIgnoreCase("deny")) {
                try {
                    if (Data.doesNumberExist(Data.SUGGESTIONS, Integer.parseInt(args[2]))) {

                        // Set suggestion instance
                        Suggestions suggestion = Data.obtainData(Data.SUGGESTIONS, Integer.parseInt(args[2]));

                        // Set it to denied
                        suggestion.setStatus("DENIED");
                        suggestion.setStatusReason(generateStatusReason(args));

                        // Build and edit denied suggestion embed
                        getSuggestionsChannel(e).editMessageById(suggestion.getId(), createSuggestionEmbed(suggestion, e)).queue();

                        final Suggestions pmSuggest = suggestion;

                        // Notify user about the results of their suggestion
                        userById(suggestion.getSuggesterId()).openPrivateChannel().queue((userChannel) ->
                        {
                            // Send a private message to the user
                            userChannel.sendMessage("**Hey there! Staff have updated the status of your suggestion:**").queue();
                            // Send suggestion embed
                            userChannel.sendMessage(createSuggestionEmbed(pmSuggest, e)).queue();
                        });

                        // Send message in channel informing executor that the command was ran
                        channel.sendMessage("**Suggestion #"+suggestion.getNumber()+" has been set to DENIED!**").queue();
                        DeleteMe.deleteQueue("**Suggestion #"+suggestion.getNumber()+" has been set to DENIED!**");

                    } else {
                        // Queue msg to be deleted
                        DeleteMe.deleteQueue(new MessageBuilder().setContent("**Suggestion not found. Please check your command!**").build());
                        // Send command not found msg
                        channel.sendMessage("**Suggestion not found. Please check your command!**").queue();
                    }
                } catch (IOException | ClassNotFoundException ex) {
                    ex.printStackTrace();
                }
            }

            // Suggestions Approve Command
            else if (args[1].equalsIgnoreCase("approve")) {
                try {
                    if (Data.doesNumberExist(Data.SUGGESTIONS, Integer.parseInt(args[2]))) {

                        Suggestions suggestion = Data.obtainData(Data.SUGGESTIONS, Integer.parseInt(args[2]));
                        // Set new data
                        try {
                            suggestion.setStatus("APPROVED");
                            suggestion.setStatusReason(generateStatusReason(args));
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                        // Build and edit denied suggestion embed
                        getSuggestionsChannel(e).editMessageById(suggestion.getId(), createSuggestionEmbed(suggestion, e)).queue();

                        // Notify user about the results of their suggestion
                        userById(suggestion.getSuggesterId()).openPrivateChannel().queue((userChannel) ->
                        {
                            // Send a private message to the user
                            userChannel.sendMessage("**Hey there! Staff have updated the status of your suggestion:**").queue();
                            // Send suggestion embed
                            userChannel.sendMessage(createSuggestionEmbed(suggestion, e)).queue();
                        });

                        // Send message in channel informing executor that the command was ran
                        channel.sendMessage("**Suggestion #"+suggestion.getNumber()+" has been set to APPROVED!**").queue();
                        DeleteMe.deleteQueue("**Suggestion #"+suggestion.getNumber()+" has been set to APPROVED!**");

                    } else {
                        // Queue msg to be deleted
                        DeleteMe.deleteQueue(new MessageBuilder().setContent("**Suggestion not found. Please check your command!**").build());
                        // Send command not found msg
                        channel.sendMessage("**Suggestion not found. Please check your command!**").queue();
                    }
                } catch (IOException | ClassNotFoundException ex) {
                    ex.printStackTrace();
                }
            }

            // Suggestions Hold Command
            else if (args[1].equalsIgnoreCase("hold")) {
                try {
                    if (Data.doesNumberExist(Data.SUGGESTIONS, Integer.parseInt(args[2]))) {

                        Suggestions suggestion = Data.obtainData(Data.SUGGESTIONS, Integer.parseInt(args[2]));
                        // Set new data
                        try {
                            suggestion.setStatus("PENDING");
                            suggestion.setStatusReason(generateStatusReason(args));
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                        // Build and edit denied suggestion embed
                        getSuggestionsChannel(e).editMessageById(suggestion.getId(), createSuggestionEmbed(suggestion, e)).queue();

                        // Notify user about the results of their suggestion
                        userById(suggestion.getSuggesterId()).openPrivateChannel().queue((userChannel) ->
                        {
                            // Send a private message to the user
                            userChannel.sendMessage("**Hey there! Staff have updated the status of your suggestion:**").queue();
                            // Send suggestion embed
                            userChannel.sendMessage(createSuggestionEmbed(suggestion, e)).queue();
                        });

                        // Send message in channel informing executor that the command was ran
                        channel.sendMessage("**Suggestion #"+suggestion.getNumber()+" has been set to PENDING!**").queue();
                        DeleteMe.deleteQueue("**Suggestion #"+suggestion.getNumber()+" has been set to PENDING!**");

                    } else {
                        // Queue msg to be deleted
                        DeleteMe.deleteQueue(new MessageBuilder().setContent("**Suggestion not found. Please check your command!**").build());
                        // Send command not found msg
                        channel.sendMessage("**Suggestion not found. Please check your command!**").queue();
                    }
                } catch (IOException | ClassNotFoundException ex) {
                    ex.printStackTrace();
                }
            }

            // If no sub commands match
            else {
                // Queue msg to be deleted
                DeleteMe.deleteQueue(getSuggestionsHelpMsg());
                // Send msg
                channel.sendMessage(getSuggestionsHelpMsg()).queue();
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
                .append("> `/Suggestion Deny <ID> (Reason)` - *Deny a suggestion*")
                .append("> `/Suggestion Hold <ID> (Reason)` - *Put a suggestion on hold*")
                .build();
    }

    private String generateStatusReason(String[] args) {
        if (args.length > 3) {
            StringBuilder string = new StringBuilder();
            for (int i = 3 ; i < args.length ; i++) {
                string.append(" ").append(args[i]);
            }
            return string.toString();
        }

        else
            return "No reason specified";
    }

}
