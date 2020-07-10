package commands;

import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import utils.MembersCache;
import utils.selfdata.ChannelData;
import utils.selfdata.SelfData;
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
import static utils.tools.GTools.stringFromArgsAfter;

public class ChannelCommand extends Command {

    public ChannelCommand() {
        super("channel", "Create and manage your own private voice channels", Rank.VIP, Type.DISCORD_ONLY);
    }

    @Override
    public void onCommandUse(Message message, Member member, GTMUser gtmUser, MessageChannel channel, String[] args) {

        if (args.length < 1) {
            GTools.sendThenDelete(channel, getCommandHelp(member));
            return;
        }

        switch (args[0].toLowerCase()) {

            case "create": {

                if (!CustomChannel.canCreateChannels()) {
                    GTools.sendThenDelete(channel, "**The custom channel category is not configured yet! Please ask an admin to configure this.**");
                    return;
                }

                if (CustomChannel.get(member).isPresent()) {
                    GTools.sendThenDelete(channel, "**You can only have `1` private channel at a time!**");
                    return;
                }

                String name;
                if (args.length == 1)
                    name = member.getEffectiveName() + "'s Private Channel";
                else name = stringFromArgsAfter(args, 1);

                CustomChannel customChannel = new CustomChannel(name, member, false);
                customChannel.create().thenAccept( (vc) -> {
                    String msg = "**Successfully created your private channel `" + vc.getName() + "`!**\n";
                    msg += customChannel.getInvite().getUrl();
                    GTools.sendThenDelete(channel, msg);
                });

                break;
            }

            /* Due to discord rate limits, this is disabled
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
                GTools.sendThenDelete(channel, "**Renaming your custom voice channel `" + optionalChannel.get().getChannelName() + "` to " + args[1] + "!**");

                break;
            }
             */

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

                if (!args[1].equalsIgnoreCase("true") && !args[1].equalsIgnoreCase("false")) {
                    GTools.sendThenDelete(channel, "**Please type either `true` to set your channel to public or `false` to set it to private.**");
                    return;
                }

                boolean publicChannel = Boolean.parseBoolean(args[0]);
                optionalChannel.get().setPublicChannel(publicChannel);
                GTools.sendThenDelete(channel, "**Setting your custom channel to " + (publicChannel ? "public" : "private") + "!**");

                break;
            }

            case "add": {

                if (args.length < 2) {
                    GTools.sendThenDelete(channel, "`/Channel Add <Member ID / Tag>` - *Add the selected discord member to your custom channel.*");
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

                break;
            }

            case "remove": {

                if (args.length < 2) {
                    GTools.sendThenDelete(channel, "`/Channel Remove <Member ID / Tag>` - *Remove the selected discord member from your custom channel.*");
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

                break;
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

                break;
            }

            case "reset": {

                Optional<CustomChannel> optionalChannel = CustomChannel.get(member);

                if (!optionalChannel.isPresent()) {
                    GTools.sendThenDelete(channel, "**You don't own a custom channel! Do `/channel create` to make one.**");
                    return;
                }

                optionalChannel.get().reset();
                GTools.sendThenDelete(channel, "**Resetting your custom channel " + optionalChannel.get().getChannelName() + " has been reset to default settings...!**");

                break;
            }

            case "delete": {

                Optional<CustomChannel> optionalChannel = CustomChannel.get(member);

                if (!optionalChannel.isPresent()) {
                    GTools.sendThenDelete(channel, "**You don't own a custom channel! Do `/channel create` to make one.**");
                    return;
                }

                optionalChannel.get().remove();
                GTools.sendThenDelete(channel, "**Deleting your custom channel " + optionalChannel.get().getChannelName() + "...!**");

                break;
            }

            case "setcategory": {

                if (!Rank.hasRolePerms(member, Rank.ADMIN)) {
                    GTools.sendThenDelete(channel, "**Sorry but only Admins+ can use this sub-command!**");
                    return;
                }

                if (args.length < 2) {
                    GTools.sendThenDelete(channel, "`/Channel SetCategory <ID>` - *Sets the selected category id as the custom channel category.*");
                    return;
                }

                long categoryId;
                try {
                    categoryId = Long.parseLong(args[1]);
                } catch (NumberFormatException e) {
                    GTools.sendThenDelete(channel, "**You provided an invalid channel id!**");
                    return;
                }

                if (guild.getCategoryById(categoryId) == null) {
                    GTools.sendThenDelete(channel, "**You provided an invalid channel id!**");
                    return;
                }

                SelfData.get().setPrivateChannelsCategory(categoryId);
                GTools.sendThenDelete(channel, "**<@" + categoryId + "> has been successfully set as the private channels category.**");

                break;
            }

            default: {
                GTools.sendThenDelete(channel, getCommandHelp(member));
                return;
            }

        }

    }

    private Message getCommandHelp(Member member) {
        MessageBuilder mb = new MessageBuilder()
                .append("> **Please enter a valid command argument:**\n")
                .append("> `/Channel Create (name)` - *Create a new custom channel with the selected name.*\n")
                //.append("> `/Channel Rename <name>` - *Rename your custom channel to the given argument.*\n")
                .append("> `/Channel SetPublic <True/False>` - *Configure whether everyone should be able to join your channel.*\n")
                .append("> `/Channel Add <Member ID / Tag>` - *Add the selected discord member to your custom channel.*\n")
                .append("> `/Channel Remove <Member ID / Tag>` - *Remove the selected discord member from your custom channel.*\n")
                .append("> `/Channel AddGang <Server Key>` - *Add all verified discord members in your gang on the specified server to this private voice channel*\n")
                .append("> `/Channel Reset` - *Reset your custom channel to default removing all whitelisted and blacklisted users.*\n")
                .append("> `/Channel Delete` - *Delete your custom channel.*\n");
                if (Rank.hasRolePerms(member, Rank.ADMIN)) {
                    mb.append("> `/Channel SetCategory <ID>` - *Sets the selected category id as the custom channel category.*\n");
                }
                return mb.build();
    }

}
