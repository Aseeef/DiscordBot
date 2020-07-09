package commands;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import utils.MembersCache;
import utils.channels.CustomChannel;
import utils.database.UsersDAO;
import utils.database.sql.BaseDatabase;
import utils.tools.GTools;
import utils.users.GTMUser;
import utils.users.Rank;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static utils.tools.GTools.guild;

public class ChannelCommand extends Command {

    public ChannelCommand() {
        super("channel", "Create and manage your own private voice channels", Rank.VIP, Type.DISCORD_ONLY);
    }

    @Override
    public void onCommandUse(Message message, Member member, GTMUser gtmUser, MessageChannel channel, String[] args) {

        if (args.length < 1) {
            //TODO COMMAND HELP
            return;
        }

        switch (args[0].toLowerCase()) {

            case "create": {

                if (CustomChannel.get(member).isPresent()) {
                    GTools.sendThenDelete(channel, "**You can only have `1` private channel at a time!**");
                    return;
                }

                String name;
                if (args.length > 2)
                    name = member.getEffectiveName() + "'s Private Channel";
                else name = args[1];

                CustomChannel customChannel = new CustomChannel(name, member, false);
                customChannel.create().thenAccept( (vc) -> {
                    String msg = "**Successfully created your private channel `" + vc.getName() + "`!**\n";
                    msg += customChannel.getInvite().getUrl();
                    GTools.sendThenDelete(channel, msg);
                });

                break;
            }

            case "rename": {

                if (args.length < 2) {
                    GTools.sendThenDelete(channel, "`/Channel Rename <Name>` - *Rename your custom voice channel*");
                    return;
                }

                Optional<CustomChannel> optionalChannel = CustomChannel.get(member);

                if (!optionalChannel.isPresent()) {
                    GTools.sendThenDelete(channel, "**You don't own a custom channel! Do `/channel create` to make one.**");
                    return;
                }

                optionalChannel.get().setChannelName(args[1]);
                GTools.sendThenDelete(channel, "**Renaming your custom voice channel `" + optionalChannel.get().getChannelName() + "` to " + args[1] + "!");

            }

            case "setpublic": {

                if (args.length < 2) {
                    GTools.sendThenDelete(channel, "`/Channel SetPublic <True/False>` - *Configure whether everyone should be able to join your channel.*");
                    return;
                }

                Optional<CustomChannel> optionalChannel = CustomChannel.get(member);

                if (!optionalChannel.isPresent()) {
                    GTools.sendThenDelete(channel, "**You don't own a custom channel! Do `/channel create` to make one.**");
                    return;
                }

                if (!args[0].equalsIgnoreCase("true") && !args[0].equalsIgnoreCase("false")) {
                    GTools.sendThenDelete(channel, "**Please type either `true` to set your channel to `public` or false to set it to private.**");
                    return;
                }

                boolean publicChannel = Boolean.parseBoolean(args[0]);
                optionalChannel.get().setPublicChannel(publicChannel);
                GTools.sendThenDelete(channel, "**Setting your custom channel to " + (publicChannel ? "public" : "private") + "!**");

            }

            case "add": {

                if (args.length < 2) {
                    GTools.sendThenDelete(channel, "`/Channel Add <Member ID / Tag>` - *Configure whether everyone should be able to join your channel.*");
                    return;
                }

                Optional<CustomChannel> optionalChannel = CustomChannel.get(member);

                if (!optionalChannel.isPresent()) {
                    GTools.sendThenDelete(channel, "**You don't own a custom channel! Do `/channel create` to make one.**");
                    return;
                }

                Optional<Member> optionalTarget = MembersCache.getMember(args[1]);

                if (!optionalTarget.isPresent()) {
                    GTools.sendThenDelete(channel, "**Target not found!**");
                    return;
                }

                Member target = optionalTarget.get();
                optionalChannel.get().addMember(target);
                GTools.sendThenDelete(channel, "**Inviting " + target.getAsMention() + " to your custom channel!**");

            }

            case "remove": {

                if (args.length < 2) {
                    GTools.sendThenDelete(channel, "`/Channel Remove <Member ID / Tag>` - *Configure whether everyone should be able to join your channel.*");
                    return;
                }

                Optional<CustomChannel> optionalChannel = CustomChannel.get(member);

                if (!optionalChannel.isPresent()) {
                    GTools.sendThenDelete(channel, "**You don't own a custom channel! Do `/channel create` to make one.**");
                    return;
                }

                Optional<Member> optionalTarget = MembersCache.getMember(args[1]);

                if (!optionalTarget.isPresent()) {
                    GTools.sendThenDelete(channel, "**Target not found!**");
                    return;
                }

                Member target = optionalTarget.get();

                optionalChannel.get().removeMember(target);

                if (target.getVoiceState() != null && target.getVoiceState().inVoiceChannel() &&
                        target.getVoiceState().getChannel().getIdLong() == optionalChannel.get().getVoiceChannel().getIdLong()) {
                    guild.kickVoiceMember(target).queue();
                }

                GTools.sendThenDelete(channel, "**" + target.getAsMention() + " has been removed from your custom voice channel!**");

            }

            case "addgang": {

                if (args.length < 2) {
                    GTools.sendThenDelete(channel, "`/Channel AddGang <Server Key>` - *Add all verified discord members in your gang on the specified server to this private voice channel*");
                    return;
                }

                if (!args[1].equalsIgnoreCase("gtm1") && !args[1].equalsIgnoreCase("gtm4") && !args[1].equalsIgnoreCase("gtm7")) {
                    GTools.sendThenDelete(channel, "**Unknown server key! Acceptable server keys are: `GTM1` [Minesantos], `GTM4` [Sanktburg], `GTM7` [New Mineport]**");
                    return;
                }

                Optional<CustomChannel> optionalChannel = CustomChannel.get(member);

                if (!optionalChannel.isPresent()) {
                    GTools.sendThenDelete(channel, "**You don't own a custom channel! Do `/channel create` to make one.**");
                    return;
                }

                if (gtmUser == null) {
                    GTools.sendThenDelete(channel, "**Your discord account is not linked to GTM so I can not find your gang members!**");
                    return;
                }

                GTools.runAsync( () -> {
                    try (Connection conn = BaseDatabase.getInstance(BaseDatabase.Database.USERS).getConnection()){
                        List<GTMUser> gangMembers = UsersDAO.getGangMembersFor(conn, gtmUser, args[1]);

                        if (gangMembers.size() == 0) {
                            GTools.sendThenDelete(channel, "**No gang members linked to the GTM Discord where found on " + args[1].toUpperCase() + " :(.**");
                            return;
                        }

                        // build msg and add members
                        StringBuilder sb = new StringBuilder("**Added ");
                        AtomicInteger i = new AtomicInteger();
                        gangMembers.forEach( gangMember -> {
                            i.getAndIncrement();
                            if (gangMember.getDiscordMember().isPresent()) {
                                optionalChannel.get().addMember(gangMember.getDiscordMember().get());
                                sb.append(gangMember.getDiscordMember().get().getAsMention());
                                if (i.get() != gangMembers.size())
                                    sb.append(", ");
                            }
                        });
                        sb.append(" to your custom channel!**");

                        GTools.sendThenDelete(channel, sb.toString());

                    } catch (SQLException e) {
                        GTools.printStackError(e);
                    }
                });

            }

            case "reset": {

                Optional<CustomChannel> optionalChannel = CustomChannel.get(member);

                if (!optionalChannel.isPresent()) {
                    GTools.sendThenDelete(channel, "**You don't own a custom channel! Do `/channel create` to make one.**");
                    return;
                }

                optionalChannel.get().reset();
                GTools.sendThenDelete(channel, "**Resetting your custom channel " + optionalChannel.get().getChannelName() + " has been reset to default settings...!**");

            }

            case "delete": {

                Optional<CustomChannel> optionalChannel = CustomChannel.get(member);

                if (!optionalChannel.isPresent()) {
                    GTools.sendThenDelete(channel, "**You don't own a custom channel! Do `/channel create` to make one.**");
                    return;
                }

                optionalChannel.get().remove();
                GTools.sendThenDelete(channel, "**Deleting your custom channel " + optionalChannel.get().getChannelName() + "...!**");

            }

            default: {
                // TODO Command help
            }

        }

    }

}
