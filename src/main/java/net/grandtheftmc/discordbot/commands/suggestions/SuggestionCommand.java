package net.grandtheftmc.discordbot.commands.suggestions;

import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.grandtheftmc.discordbot.commands.Command;
import net.grandtheftmc.discordbot.utils.Data;
import net.grandtheftmc.discordbot.utils.Utils;
import net.grandtheftmc.discordbot.utils.selfdata.ChannelIdData;
import net.grandtheftmc.discordbot.utils.users.GTMUser;
import net.grandtheftmc.discordbot.utils.users.Rank;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.List;

import static net.grandtheftmc.discordbot.utils.Utils.sendThenDelete;
import static net.grandtheftmc.discordbot.utils.Utils.userById;

public class SuggestionCommand extends Command {

    public SuggestionCommand() {
        super("suggestion", "Manage player suggestions", Rank.ADMIN, Type.DISCORD_ONLY);
    }

    @Override
    public void buildCommandData(SlashCommandData slashCommandData) {

    }

    @Override
    public void onCommandUse(SlashCommandInteraction interaction, MessageChannel channel, List<OptionMapping> arguments, Member member, GTMUser gtmUser, String[] path) {
        // If there are no command arguments send sub command help list
        if (path.length == 0) {
            Utils.sendThenDelete(channel, getSuggestionsHelpMsg());
        }

        // Suggestions SetChannel Command
        else if (path[0].equalsIgnoreCase("setchannel")) {

            // Set suggestions settings
            ChannelIdData.get().setSuggestionChannelId(channel.getIdLong());

            Utils.sendThenDelete(channel, suggestChannelSet((TextChannel) channel));

            // Send how to make a suggestion instruction
            SuggestionTools.suggestionInstruct((TextChannel) channel);

        }

        else if (path[0].equalsIgnoreCase("delete")) {
            if (Data.doesDataExist(Data.SUGGESTIONS, Integer.parseInt(path[1]))) {
                Suggestions suggestion = (Suggestions) Data.obtainData(Data.SUGGESTIONS, Integer.parseInt(path[1]));
                SuggestionTools.getSuggestionsChannel().deleteMessageById(suggestion.getId()).queue();
                Data.deleteData(Data.SUGGESTIONS, Integer.parseInt(path[1]));
                Utils.sendThenDelete(channel, "**Suggestion #" + Integer.parseInt(path[1]) + " has been deleted!**");
            }
            else {
                Utils.sendThenDelete(channel, "**Suggestion not found. Please check your command!**");
            }
        }

        // Suggestions Deny Command
        else if (path[0].equalsIgnoreCase("deny")) {
            if (Data.doesDataExist(Data.SUGGESTIONS, Integer.parseInt(path[1]))) {

                // Get suggestion instance
                Suggestions suggestion = (Suggestions) Data.obtainData(Data.SUGGESTIONS, Integer.parseInt(path[1]));

                // Set it to denied
                suggestion.setStatus("DENIED");
                suggestion.setStatusReason(generateStatusReason(path));

                // Build and edit denied suggestion embed
                SuggestionTools.getSuggestionsChannel().editMessageEmbedsById(suggestion.getId(), SuggestionTools.createSuggestionEmbed(suggestion)).queue();

                final Suggestions pmSuggest = suggestion;

                // Notify user about the results of their suggestion
                Utils.userById(suggestion.getSuggesterId()).openPrivateChannel().queue((userChannel) ->
                {
                    // Send a private message to the user
                    userChannel.sendMessage("**Hey there! Staff have updated the status of your suggestion:**").queue();
                    // Send suggestion embed
                    userChannel.sendMessageEmbeds(SuggestionTools.createSuggestionEmbed(pmSuggest)).queue();
                });

                // Send message in channel informing executor that the command was ran
                Utils.sendThenDelete(channel, "**Suggestion #"+suggestion.getNumber()+" has been set to DENIED!**");

            }
            // If none of the suggestions match provided id
            else {
                Utils.sendThenDelete(channel, "**Suggestion not found. Please check your command!**");
            }
        }

        // Suggestions Approve Command
        else if (path[0].equalsIgnoreCase("approve")) {
            if (Data.doesDataExist(Data.SUGGESTIONS, Integer.parseInt(path[1]))) {

                Suggestions suggestion = (Suggestions) Data.obtainData(Data.SUGGESTIONS, Integer.parseInt(path[1]));
                // Set new data
                suggestion.setStatus("APPROVED");
                suggestion.setStatusReason(generateStatusReason(path));
                // Build and edit denied suggestion embed
                SuggestionTools.getSuggestionsChannel().editMessageEmbedsById(suggestion.getId(), SuggestionTools.createSuggestionEmbed(suggestion)).queue();

                // Notify user about the results of their suggestion
                Utils.userById(suggestion.getSuggesterId()).openPrivateChannel().queue((userChannel) ->
                {
                    // Send a private message to the user
                    userChannel.sendMessage("**Hey there! Staff have updated the status of your suggestion:**").queue();
                    // Send suggestion embed
                    userChannel.sendMessageEmbeds(SuggestionTools.createSuggestionEmbed(suggestion)).queue();
                });

                // Send message in channel informing executor that the command was ran
                Utils.sendThenDelete(channel, "**Suggestion #"+suggestion.getNumber()+" has been set to APPROVED!**");

            }
            // If none of the suggestions match provided id
            else {
                Utils.sendThenDelete(channel, "**Suggestion not found. Please check your command!**");
            }
        }

        // Suggestions Approve Command
        else if (path[0].equalsIgnoreCase("complete")) {
            if (Data.doesDataExist(Data.SUGGESTIONS, Integer.parseInt(path[1]))) {

                Suggestions suggestion = (Suggestions) Data.obtainData(Data.SUGGESTIONS, Integer.parseInt(path[1]));
                // Set new data
                suggestion.setStatus("COMPLETED");
                suggestion.setStatusReason(generateStatusReason(path));
                // Build and edit denied suggestion embed
                SuggestionTools.getSuggestionsChannel().editMessageEmbedsById(suggestion.getId(), SuggestionTools.createSuggestionEmbed(suggestion)).queue();

                // Notify user about the results of their suggestion
                Utils.userById(suggestion.getSuggesterId()).openPrivateChannel().queue((userChannel) ->
                {
                    // Send a private message to the user
                    userChannel.sendMessage("**Hey there! Staff have updated the status of your suggestion:**").queue();
                    // Send suggestion embed
                    userChannel.sendMessageEmbeds(SuggestionTools.createSuggestionEmbed(suggestion)).queue();
                });

                // Send message in channel informing executor that the command was ran
                Utils.sendThenDelete(channel, "**Suggestion #"+suggestion.getNumber()+" has been set to COMPLETED!**");

            }
            // If none of the suggestions match provided id
            else {
                Utils.sendThenDelete(channel, "**Suggestion not found. Please check your command!**");
            }
        }

        // Suggestions Hold Command
        else if (path[0].equalsIgnoreCase("hold")) {
            if (Data.doesDataExist(Data.SUGGESTIONS, Integer.parseInt(path[1]))) {

                Suggestions suggestion = (Suggestions) Data.obtainData(Data.SUGGESTIONS, Integer.parseInt(path[1]));
                // Set new data
                suggestion.setStatus("PENDING");
                suggestion.setStatusReason(generateStatusReason(path));
                // Build and edit denied suggestion embed
                SuggestionTools.getSuggestionsChannel().editMessageEmbedsById(suggestion.getId(), SuggestionTools.createSuggestionEmbed(suggestion)).queue();

                // Notify user about the results of their suggestion
                Utils.userById(suggestion.getSuggesterId()).openPrivateChannel().queue((userChannel) ->
                {
                    // Send a private message to the user
                    userChannel.sendMessage("**Hey there! Staff have updated the status of your suggestion:**").queue();
                    // Send suggestion embed
                    userChannel.sendMessageEmbeds(SuggestionTools.createSuggestionEmbed(suggestion)).queue();
                });

                // Send message in channel informing executor that the command was ran
                Utils.sendThenDelete(channel, "**Suggestion #"+suggestion.getNumber()+" has been set to PENDING!**");

            }
            // If none of the suggestions match provided id
            else {
                Utils.sendThenDelete(channel, "**Suggestion not found. Please check your command!**");
            }
        }

        // If no sub commands match
        else {
            Utils.sendThenDelete(channel, getSuggestionsHelpMsg());
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
                .append("> `/Suggestion Delete <ID>` - *Deletes selected suggestion*\n")
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
