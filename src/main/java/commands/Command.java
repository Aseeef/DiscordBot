package commands;

import Utils.Config;
import Utils.Rank;
import Utils.tools.GTools;
import Utils.users.GTMUser;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.ArrayList;
import java.util.List;

import static Utils.tools.GTools.jda;
import static Utils.tools.GTools.sendThenDelete;
import static Utils.console.Logs.log;

public abstract class Command extends ListenerAdapter {

    private String name;
    private String description;
    private Rank rank;
    private Type type;

    private static List<Command> commandList = new ArrayList<>();

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

    public static List<Command> getCommands() {
        return commandList;
    }

    public static Command getByName (String commandName) {
        return commandList.stream().filter( (command) -> command.getName().equals(commandName)).findFirst().orElse(null);
    }

    @Override
    public void onPrivateMessageReceived(PrivateMessageReceivedEvent e) {

        String msg = e.getMessage().getContentRaw();
        User user = e.getAuthor();
        String[] args = getArgs(msg);
        PrivateChannel channel = e.getChannel();
        GTMUser gtmUser = GTMUser.getGTMUser(user.getIdLong()).orElse(null);

        jda.getGuilds().get(0).retrieveMember(user).queue( (member -> {
            if (GTools.isCommand(msg, user, name)) {

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

            }
        }));

    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent e) {

        String msg = e.getMessage().getContentRaw();
        Member member = e.getMember();
        String[] args = getArgs(msg);
        TextChannel channel = e.getChannel();

        if (member != null && GTools.isCommand(msg, member.getUser(), name)) {

            log("User "+ e.getAuthor().getAsTag()+
                    "("+e.getAuthor().getId()+") issued command: " + e.getMessage().getContentRaw());

            GTMUser gtmUser = GTMUser.getGTMUser(member.getIdLong()).orElse(null);

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

        }

    }

    /** This is the logic that occurs when this command is used
     * Note: To use TextChannel or PrivateChannel methods, use casting
     */
    public abstract void onCommandUse(Message message, Member member, GTMUser gtmUser, MessageChannel channel, String[] args);

    enum Type {
        /** Commands of this type can be executed anywhere by the user */
        ANYWHERE,
        /** Commands of this type can only be executed in the bot's direct messages */
        DMS_ONLY,
        /** Commands of this type can only be executed in the GTM discord */
        DISCORD_ONLY,
    }

    public static String[] getArgs(String msg) {
        if (msg.split(" ").length == 1) return new String[0];
        else return msg.replaceFirst(Config.get().getCommandPrefix() + "[^ ]+ ", "").split(" ");
    }

}
