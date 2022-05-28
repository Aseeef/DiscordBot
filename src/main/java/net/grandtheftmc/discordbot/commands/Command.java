package net.grandtheftmc.discordbot.commands;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;
import net.grandtheftmc.discordbot.GTMBot;
import org.jetbrains.annotations.NotNull;
import net.grandtheftmc.discordbot.utils.confighelpers.Config;
import net.grandtheftmc.discordbot.utils.users.GTMUser;
import net.grandtheftmc.discordbot.utils.users.Rank;

import java.util.*;

import static net.grandtheftmc.discordbot.utils.console.Logs.log;

public abstract class Command extends ListenerAdapter {

    private final static int COMMAND_MS_DELAY = 1250;

    private final CommandDataImpl commandData;

    private final String name;
    private final String description;
    private final Rank rank;
    private final Type type;

    private static final List<Command> commandList = new ArrayList<>();

    private static final Map<User, Long> antiSpamMap = new HashMap<>();

    /** The constructor for discord commands for the GTM bot
     *
     * @param name - The name of the command as used in the command (eg /name)
     * @param description - The command description displayed in /help
     * @param rank - The rank required to use this rank
     */
    public Command(String name, String description, Rank rank, Type type, OptionData... argUsage) {
        this.name = name;
        this.description = description;
        this.rank = rank;
        this.type = type;
        commandList.add(this);

        this.commandData = new CommandDataImpl(name.toLowerCase(), description);
        this.commandData.addOptions(argUsage);
        GTMBot.getJDA().getGuilds().forEach(g -> g.upsertCommand(this.commandData).queue());
    }

    @Override
    public void onSlashCommandInteraction (@NotNull SlashCommandInteractionEvent e) {

        e.getHook().setEphemeral(false); // msgs visible to everyone

        User user = e.getUser();
        Member member = e.getMember();
        GTMUser gtmUser = GTMUser.getGTMUser(user.getIdLong()).orElse(null);
        MessageChannel channel = e.getChannel();
        String commandMessage = e.getInteraction().getCommandString();
        String[] args = commandMessage.split(" ");

        if (e.getName().equals(name)) {

            log("User " + user.getAsTag() +
                    "(" + user.getId() + ") issued command: " + commandMessage);

            // Check perms
            if (!Rank.hasRolePerms(member, rank)) {
                e.reply("**Sorry but you must be " + getRank().name() + " or higher to use this command! Use `/help` to list all commands you can use.**").setEphemeral(true).queue();
                return;
            }

            // Check type
            if (type == Type.DISCORD_ONLY && e.getChannelType() == ChannelType.PRIVATE) {
                e.reply("**Sorry but this command can only be executed on the GTM discord!**").setEphemeral(true).queue();
                return;
            } else if (type == Type.DMS_ONLY && e.getChannelType() != ChannelType.PRIVATE) {
                e.reply("**Sorry but this command can only be executed in direct messages with me!**").setEphemeral(true).queue();
                return;
            }

            // acknowledge command
            e.deferReply(true).queue();

            onCommandUse(e.getInteraction(), channel, member, gtmUser, args);
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

    /** This is the logic that occurs when this command is used
     * Note: To use TextChannel or PrivateChannel methods, use casting
     */
    public abstract void onCommandUse(SlashCommandInteraction interaction, MessageChannel channel, Member member, GTMUser gtmUser, String[] args);

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
