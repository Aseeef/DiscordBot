package net.grandtheftmc.discordbot.commands;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.grandtheftmc.discordbot.GTMBot;
import net.grandtheftmc.discordbot.utils.confighelpers.Config;
import net.grandtheftmc.discordbot.utils.users.GTMUser;
import net.grandtheftmc.discordbot.utils.users.Rank;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static net.grandtheftmc.discordbot.utils.console.Logs.log;

public abstract class Command extends ListenerAdapter {

    private final SlashCommandData commandData;

    private final String name;
    private final String description;
    private final Rank rank;
    private final Type type;

    private static final List<Command> commandList = new ArrayList<>();

    /** The constructor for discord commands for the GTM bot
     *
     * @param name - The name of the command as used in the command (eg /name)
     * @param description - The command description displayed in /help
     * @param rank - The rank required to use this rank
     */
    public Command(String name, String description, Rank rank, Type type) {
        this.name = name;
        this.description = description;
        this.rank = rank;
        this.type = type;
        commandList.add(this);

        this.commandData = Commands.slash(name.toLowerCase(), description);
        this.buildCommandData(this.commandData);
        this.commandData.setDefaultEnabled(false);

        GTMBot.getJDA().addEventListener(this);
    }

    @Override
    public void onSlashCommandInteraction (@NotNull SlashCommandInteractionEvent e) {

        User user = e.getUser();
        Member member = e.getMember();
        GTMUser gtmUser = GTMUser.getGTMUser(user.getIdLong()).orElse(null);
        MessageChannel channel = e.getChannel();
        String[] path = e.getInteraction().getCommandPath().split("/");

        if (e.getName().equalsIgnoreCase(name)) {

            log("User " + user.getAsTag() +
                    "(" + user.getId() + ") issued command: " + e.getCommandString());

            // Check perms
            if (!Rank.hasRolePerms(member, rank)) {
                e.getHook().setEphemeral(true);
                e.reply("**Sorry but you must be " + getRank().name() + " or higher to use this command! Use `/help` to list all commands you can use.**").setEphemeral(true).queue();
                return;
            }

            // Check type
            if (type == Type.DISCORD_ONLY && e.getChannelType() == ChannelType.PRIVATE) {
                e.getHook().setEphemeral(true);
                e.reply("**Sorry but this command can only be executed on the GTM discord!**").setEphemeral(true).queue();
                return;
            } else if (type == Type.DMS_ONLY && e.getChannelType() != ChannelType.PRIVATE) {
                e.getHook().setEphemeral(true);
                e.reply("**Sorry but this command can only be executed in direct messages with me!**").setEphemeral(true).queue();
                return;
            }

            e.getHook().setEphemeral(false); // msgs visible to everyone by default
            onCommandUse(e.getInteraction(), channel, e.getOptions(), member, gtmUser, path);
        }

    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Rank getRank() {
        return rank;
    }

    public Type getType() {
        return type;
    }

    public CommandData getCommandData() {
        return commandData;
    }

    public static List<Command> getCommands() {
        return commandList;
    }

    public static Command getByName (String commandName) {
        return commandList.stream().filter( (command) -> command.getName().equals(commandName)).findFirst().orElse(null);
    }

    public List<Role> getPrivilegedRoles() {
        return this.rank.getRolesAbove();
    }

    /**
     * Build the slash command data such as accepted command arguments
     */
    public abstract void buildCommandData(SlashCommandData slashCommandData);

    /** This is the logic that occurs when this command is used
     * Note: To use TextChannel or PrivateChannel methods, use casting
     */
    public abstract void onCommandUse(SlashCommandInteraction interaction, MessageChannel channel, List<OptionMapping> arguments, Member member, GTMUser gtmUser, String[] path);

    // -- Commands static methods -- //

    public static String[] getArgs(String msg) {
        if (msg.split(" ").length == 1) return new String[0];
        else return msg.replaceFirst(Config.get().getCommandPrefix() + "[^ ]+ ", "").split(" ");
    }

    public static boolean ifCommandExists(String commandString) {
        // Checks if its is a specific command
        Optional<Command> optionalCommand = getCommands().stream().filter( (command) -> command.getName().equalsIgnoreCase(commandString)).findFirst();
        return optionalCommand.isPresent();
    }

    public enum Type {
        /** Commands of this type can be executed anywhere by the user */
        ANYWHERE,
        /** Commands of this type can only be executed in the bot's direct messages */
        DMS_ONLY,
        /** Commands of this type can only be executed in the GTM discord */
        DISCORD_ONLY,
    }

}
