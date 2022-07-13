package net.grandtheftmc.discordbot.commands.suggestions;

import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
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

public class SuggestAdminCommand extends Command {

    public SuggestAdminCommand() {
        super("suggestadmin", "Manage player suggestions", Rank.ADMIN, Type.DISCORD_ONLY);
    }

    @Override
    public void buildCommandData(SlashCommandData slashCommandData) {

        SubcommandData setChannel = new SubcommandData("setchannel", "Set current channel to the suggestion channel");

        SubcommandData delete = new SubcommandData("delete", "Deletes selected suggestion");
        delete.addOption(OptionType.INTEGER, "suggestion-id", "The integer id for the suggestion");

        SubcommandData deny = new SubcommandData("approve", "Denies selected suggestion");
        deny.addOption(OptionType.INTEGER, "suggestion-id", "The integer id for the suggestion", true);
        deny.addOption(OptionType.STRING, "reason", "The reason for this decision (optional)", false);

        SubcommandData approve = new SubcommandData("deny", "Approves selected suggestion");
        approve.addOption(OptionType.INTEGER, "suggestion-id", "The integer id for the suggestion", true);
        approve.addOption(OptionType.STRING, "reason", "The reason for this decision (optional)", false);

        SubcommandData complete = new SubcommandData("complete", "Marks the selected suggestion as completed");
        complete.addOption(OptionType.INTEGER, "suggestion-id", "The integer id for the suggestion", true);
        complete.addOption(OptionType.STRING, "reason", "The reason for this decision (optional)", false);

        SubcommandData hold = new SubcommandData("hold", "Marks the selected suggestion as on hold");
        hold.addOption(OptionType.INTEGER, "suggestion-id", "The integer id for the suggestion", true);
        hold.addOption(OptionType.STRING, "reason", "The reason for this decision (optional)", false);

        slashCommandData.addSubcommands(setChannel, delete, deny, approve, complete, hold);
    }

    @Override
    public void onCommandUse(SlashCommandInteraction interaction, MessageChannel channel, List<OptionMapping> arguments, Member member, GTMUser gtmUser, String[] path) {

        interaction.getHook().setEphemeral(true);

        // Suggestions SetChannel Command
        if (path[0].equalsIgnoreCase("setchannel")) {

            // Set suggestions settings
            ChannelIdData.get().setSuggestionChannelId(channel.getIdLong());

            interaction.reply(suggestChannelSet((TextChannel) channel)).setEphemeral(true).queue();

            // Send how to make a suggestion instruction
            SuggestionTools.suggestionInstruct((TextChannel) channel);

        }

        else if (path[0].equalsIgnoreCase("delete")) {
            int suggestionId = interaction.getOption("suggestion-id").getAsInt();
            if (Data.doesDataExist(Data.SUGGESTIONS, suggestionId)) {
                Suggestions suggestion = (Suggestions) Data.obtainData(Data.SUGGESTIONS, suggestionId);
                SuggestionTools.getSuggestionsChannel().deleteMessageById(suggestion.getId()).queue();
                Data.deleteData(Data.SUGGESTIONS, Integer.parseInt(path[1]));
                interaction.reply("**Suggestion #" + Integer.parseInt(path[1]) + " has been deleted!**").setEphemeral(true).queue();
            }
            else {
                interaction.reply("**Suggestion not found. Please check your command!**").setEphemeral(true).queue();
            }
        }

        // Suggestions Deny Command
        else if (path[0].equalsIgnoreCase("deny")) {
            int suggestionId = interaction.getOption("suggestion-id").getAsInt();
            if (Data.doesDataExist(Data.SUGGESTIONS, suggestionId)) {

                // Get suggestion instance
                Suggestions suggestion = (Suggestions) Data.obtainData(Data.SUGGESTIONS, suggestionId);

                // Set it to denied
                suggestion.setStatus("DENIED");
                String reason = interaction.getOption("reason") == null ? "No reason specified" : interaction.getOption("reason").getAsString();
                suggestion.setStatusReason(reason);

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
                interaction.reply("**Suggestion #"+suggestion.getNumber()+" has been set to DENIED!**").setEphemeral(true).queue();

            }
            // If none of the suggestions match provided id
            else {
                interaction.reply("**Suggestion not found. Please check your command!**").setEphemeral(true).queue();
            }
        }

        // Suggestions Approve Command
        else if (path[0].equalsIgnoreCase("approve")) {
            int suggestionId = interaction.getOption("suggestion-id").getAsInt();
            if (Data.doesDataExist(Data.SUGGESTIONS, suggestionId)) {

                Suggestions suggestion = (Suggestions) Data.obtainData(Data.SUGGESTIONS, suggestionId);
                // Set new data
                suggestion.setStatus("APPROVED");
                String reason = interaction.getOption("reason") == null ? "No reason specified" : interaction.getOption("reason").getAsString();
                suggestion.setStatusReason(reason);
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
                interaction.reply("**Suggestion #"+suggestion.getNumber()+" has been set to APPROVED!**").setEphemeral(true).queue();

            }
            // If none of the suggestions match provided id
            else {
                interaction.reply("**Suggestion not found. Please check your command!**").setEphemeral(true).queue();
            }
        }

        // Suggestions Approve Command
        else if (path[0].equalsIgnoreCase("complete")) {
            int suggestionId = interaction.getOption("suggestion-id").getAsInt();
            if (Data.doesDataExist(Data.SUGGESTIONS, suggestionId)) {

                Suggestions suggestion = (Suggestions) Data.obtainData(Data.SUGGESTIONS, suggestionId);
                // Set new data
                suggestion.setStatus("COMPLETED");
                String reason = interaction.getOption("reason") == null ? "No reason specified" : interaction.getOption("reason").getAsString();
                suggestion.setStatusReason(reason);

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
                interaction.reply("**Suggestion #"+suggestion.getNumber()+" has been set to COMPLETED!**").setEphemeral(true).queue();

            }
            // If none of the suggestions match provided id
            else {
                interaction.reply("**Suggestion not found. Please check your command!**").setEphemeral(true).queue();
            }
        }

        // Suggestions Hold Command
        else if (path[0].equalsIgnoreCase("hold")) {
            int suggestionId = interaction.getOption("suggestion-id").getAsInt();
            if (Data.doesDataExist(Data.SUGGESTIONS, suggestionId)) {

                Suggestions suggestion = (Suggestions) Data.obtainData(Data.SUGGESTIONS, suggestionId);
                // Set new data
                suggestion.setStatus("PENDING");
                String reason = interaction.getOption("reason") == null ? "No reason specified" : interaction.getOption("reason").getAsString();
                suggestion.setStatusReason(reason);
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
                interaction.reply( "**Suggestion #"+suggestion.getNumber()+" has been set to PENDING!**").setEphemeral(true).queue();

            }
            // If none of the suggestions match provided id
            else {
                interaction.reply("**Suggestion not found. Please check your command!**").setEphemeral(true).queue();
            }
        }

    }

    private static Message suggestChannelSet(TextChannel channel) {
        return new MessageBuilder().setContent(
                "**" + channel.getAsMention() + " has been set as the suggestions channel!**"
        ).build();
    }

}
