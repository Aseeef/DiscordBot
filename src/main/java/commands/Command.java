package commands;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;
import utils.Utils;
import utils.confighelpers.Config;
import utils.users.GTMUser;
import utils.users.Rank;

import java.util.*;

import static utils.Utils.JDA;
import static utils.Utils.sendThenDelete;
import static utils.console.Logs.log;

public abstract class Command extends ListenerAdapter {

    private final static int COMMAND_MS_DELAY = 1250;

    private CommandData commandData;

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

        this.commandData = new CommandData(name.toLowerCase(), description);
    }

    //todo discord interaction commands

    @Override
    public void onSlashCommand (@NotNull SlashCommandEvent e) {

        e.deferReply(true).queue(); // acknowledge command
        e.getHook().setEphemeral(false); // msgs visible to everyone

        User user = e.getUser();
        Member member = e.getMember();
        GTMUser gtmUser = GTMUser.getGTMUser(user.getIdLong()).orElse(null);
        MessageChannel channel = e.getChannel();

        if (e.getName().equals(name)) {

            log("User " + user.getAsTag() +
                    "(" + user.getId() + ") issued command: " + e.getCommandPath());

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

            //onCommandUse(e, member, gtmUser, channel, e.getOptions());
        }

    }

    @Override
    public void onPrivateMessageReceived(PrivateMessageReceivedEvent e) {

        String msg = e.getMessage().getContentRaw();
        User user = e.getAuthor();
        GTMUser gtmUser = GTMUser.getGTMUser(user.getIdLong()).orElse(null);
        String[] args = getArgs(msg);
        PrivateChannel channel = e.getChannel();

        JDA.getGuilds().get(0).retrieveMember(user).queue( (member -> {
            if (Utils.isCommand(msg, user, name)) {

                // anti command spam
                if (antiSpamMap.containsKey(e.getAuthor()) && antiSpamMap.get(e.getAuthor()) > System.currentTimeMillis() - COMMAND_MS_DELAY) {
                    sendThenDelete(channel, "**Slow down! Please do not spam commands!**");
                    return;
                }

                log("User "+ e.getAuthor().getAsTag()+
                        "("+e.getAuthor().getId()+") issued command: " + e.getMessage().getContentRaw());

                // Check perms
                if (!Rank.hasRolePerms(member, rank)) {
                    sendThenDelete(channel, "**Sorry but you must be " + getRank().name() + " or higher to use this command! Use `/help` to list all commands you can use.**");
                    return;
                }

                // Check type
                if (type == Type.DISCORD_ONLY) {
                    sendThenDelete(channel, "**Sorry but this command can only be executed on the GTM discord!**");
                    return;
                }

                onCommandUse(e.getMessage(), member, gtmUser, channel, args);
                antiSpamMap.put(e.getAuthor(), System.currentTimeMillis()); // set last command use

            }
        }));

    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent e) {

        String msg = e.getMessage().getContentRaw();
        Member member = e.getMember();
        String[] args = getArgs(msg);
        TextChannel channel = e.getChannel();

        if (member == null)
            return;

        GTMUser gtmUser = GTMUser.getGTMUser(member.getIdLong()).orElse(null);

        if (Utils.isCommand(msg, member.getUser(), name)) {

            // anti command spam
            if (antiSpamMap.containsKey(e.getAuthor()) && antiSpamMap.get(e.getAuthor()) > System.currentTimeMillis() - COMMAND_MS_DELAY) {
                sendThenDelete(channel, "**Slow down! Please do not spam commands!**");
                return;
            }

            log("User "+ e.getAuthor().getAsTag()+
                    "("+e.getAuthor().getId()+") issued command: " + e.getMessage().getContentRaw());

            // Check perms
            if (!Rank.hasRolePerms(member, rank)) {
                sendThenDelete(channel, "**Sorry but you don't have permission to use that command! Use `/help` to list all commands you can use.**");
                return;
            }

            // Check type
            if (type == Type.DMS_ONLY) {
                sendThenDelete(channel, "**Sorry but this command can only be executed in direct messages with me!**");
                return;
            }

            onCommandUse(e.getMessage(), member, gtmUser, channel, args);
            antiSpamMap.put(e.getAuthor(), System.currentTimeMillis()); // set last command use

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
    public abstract void onCommandUse(Message message, Member member, GTMUser gtmUser, MessageChannel channel, String[] args);
    //public abstract ReplyAction onCommandUse(SlashCommandEvent slashCommandEvent, Member member, GTMUser gtmUser, MessageChannel channel, List<OptionMapping> args);

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

    public static class CommandsTools extends ListenerAdapter {
        public void onGuildMessageReceived(GuildMessageReceivedEvent e) {
            onUnknownCommand(e.getMessage(), e.getChannel(), e.getAuthor());
        }
        public void onPrivateMessageReceived(PrivateMessageReceivedEvent e) {
            onUnknownCommand(e.getMessage(), e.getChannel(), e.getAuthor());
        }

        private void onUnknownCommand(Message message, MessageChannel channel, User user) {
            String msg = message.getContentRaw();
            String commandName = msg.toLowerCase().replaceFirst(Config.get().getCommandPrefix(), "")
                    .split(" ")[0];

            // If is any command, delete it
            if (Utils.isCommand(msg, user)) {
                if (channel instanceof TextChannel)
                    message.delete().queue();

                // If is unknown command, print msg
                if (!Command.ifCommandExists(commandName)) {
                    log("User "+ user.getAsTag()+
                            "("+user.getId()+") issued unknown command: " + msg);

                    Utils.sendThenDelete(channel, "**Unknown command! Do `/help` for a list of all commands that you can use!**");
                }
            }
        }

    }

}
