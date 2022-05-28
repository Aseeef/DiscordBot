package net.grandtheftmc.discordbot.commands;

import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.grandtheftmc.discordbot.utils.MembersCache;
import net.grandtheftmc.discordbot.utils.channels.CustomChannel;
import net.grandtheftmc.discordbot.utils.database.UsersDAO;
import net.grandtheftmc.discordbot.utils.database.sql.BaseDatabase;
import net.grandtheftmc.discordbot.utils.selfdata.ChannelIdData;
import net.grandtheftmc.discordbot.utils.Utils;
import net.grandtheftmc.discordbot.utils.threads.ThreadUtil;
import net.grandtheftmc.discordbot.utils.users.GTMUser;
import net.grandtheftmc.discordbot.utils.users.Rank;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static net.grandtheftmc.discordbot.utils.Utils.guild;
import static net.grandtheftmc.discordbot.utils.Utils.stringFromArgsAfter;

public class ChannelCommand extends Command {

    public ChannelCommand() {
        super("channel", "Create and manage your own private voice channels", Rank.VIP, Type.DISCORD_ONLY);
    }

    @Override
    public void buildCommandData(SlashCommandData slashCommandData) {

    }

    @Override
    public void onCommandUse(SlashCommandInteraction interaction, MessageChannel channel, List<OptionMapping> arguments, Member member, GTMUser gtmUser, String[] path) {

        if (path.length < 1) {
            Utils.sendThenDelete(channel, getCommandHelp(member));
            return;
        }

        switch (path[0].toLowerCase()) {

            case "create": {

                if (!CustomChannel.canCreateChannels()) {
                    Utils.sendThenDelete(channel, "**The custom channel category is not configured yet! Please ask an admin to configure this.**");
                    return;
                }

                if (CustomChannel.get(member).isPresent()) {
                    Utils.sendThenDelete(channel, "**You can only have `1` private channel at a time!**");
                    return;
                }

                String name;
                if (path.length == 1)
                    name = member.getEffectiveName() + "'s Private Channel";
                else name = stringFromArgsAfter(path, 1);

                CustomChannel customChannel = new CustomChannel(name, member, false);
                customChannel.create().thenAccept( (vc) -> {
                    String msg = "**Successfully created your private channel `" + vc.getName() + "`!**\n";
                    msg += customChannel.getInvite().getUrl();
                    Utils.sendThenDelete(channel, msg);
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

                if (path.length < 2) {
                    Utils.sendThenDelete(channel, "`/Channel SetPublic <True/False>` - *Configure whether everyone should be able to join your channel.*");
                    return;
                }

                Optional<CustomChannel> optionalChannel = CustomChannel.get(member);

                if (!optionalChannel.isPresent()) {
                    Utils.sendThenDelete(channel, "**You don't own a custom channel! Do `/channel create` to make one.**");
                    return;
                }

                if (!path[1].equalsIgnoreCase("true") && !path[1].equalsIgnoreCase("false")) {
                    Utils.sendThenDelete(channel, "**Please type either `true` to set your channel to public or `false` to set it to private.**");
                    return;
                }

                boolean publicChannel = Boolean.parseBoolean(path[1]);
                optionalChannel.get().setPublicChannel(publicChannel);
                Utils.sendThenDelete(channel, "**Setting your custom channel to " + (publicChannel ? "public" : "private") + "!**");

                break;
            }

            case "setmax": {

                if (path.length < 2) {
                    Utils.sendThenDelete(channel, "`/Channel SetMax <Limit>` - *Set the maximum about of people allowed in your channel; 0 is unlimited.*");
                    return;
                }

                Optional<CustomChannel> optionalChannel = CustomChannel.get(member);

                if (!optionalChannel.isPresent()) {
                    Utils.sendThenDelete(channel, "**You don't own a custom channel! Do `/channel create` to make one.**");
                    return;
                }

                int limit;
                try {
                    limit = Integer.parseInt(path[1]);
                } catch (NumberFormatException e) {
                    Utils.sendThenDelete(channel, "**`" + path[1] + "` is not a number!**");
                    return;
                }

                optionalChannel.get().setChannelMax(limit);
                Utils.sendThenDelete(channel, "**Setting max channel user limit to " + path[1] + " users...!**");

                break;
            }

            case "add": {

                if (path.length < 2) {
                    Utils.sendThenDelete(channel, "`/Channel Add <Member ID / Tag>` - *Add the selected discord member to your custom channel.*");
                    return;
                }

                Optional<CustomChannel> optionalChannel = CustomChannel.get(member);

                if (!optionalChannel.isPresent()) {
                    Utils.sendThenDelete(channel, "**You don't own a custom channel! Do `/channel create` to make one.**");
                    return;
                }

                Optional<Member> optionalTarget = MembersCache.getMember(path[1]);

                if (!optionalTarget.isPresent()) {
                    Utils.sendThenDelete(channel, "**Target not found!**");
                    return;
                }

                Member target = optionalTarget.get();

                if (target == member) {
                    Utils.sendThenDelete(channel, "**You can't add yourself to your channel because you own it!**");
                    return;
                }

                optionalChannel.get().addMember(target);
                Utils.sendThenDelete(channel, "**Inviting " + target.getAsMention() + " to your custom channel!**");

                break;
            }

            case "remove": {

                if (path.length < 2) {
                    Utils.sendThenDelete(channel, "`/Channel Remove <Member ID / Tag>` - *Remove the selected discord member from your custom channel.*");
                    return;
                }

                Optional<CustomChannel> optionalChannel = CustomChannel.get(member);

                if (!optionalChannel.isPresent()) {
                    Utils.sendThenDelete(channel, "**You don't own a custom channel! Do `/channel create` to make one.**");
                    return;
                }

                Optional<Member> optionalTarget = MembersCache.getMember(path[1]);

                if (!optionalTarget.isPresent()) {
                    Utils.sendThenDelete(channel, "**Target not found!**");
                    return;
                }

                Member target = optionalTarget.get();

                if (target == member) {
                    Utils.sendThenDelete(channel, "**You can't remove yourself from your own channel!**");
                    return;
                }

                if (Rank.hasRolePerms(target, Rank.ADMIN)) {
                    Utils.sendThenDelete(channel, "**Sorry but it is not possible to block admins from your custom channel!**");
                    return;
                }

                optionalChannel.get().removeMember(target);

                if (target.getVoiceState() != null && target.getVoiceState().inAudioChannel() &&
                        target.getVoiceState().getChannel().getIdLong() == optionalChannel.get().getVoiceChannel().getIdLong()) {
                    guild.kickVoiceMember(target).queue();
                }

                Utils.sendThenDelete(channel, "**" + target.getAsMention() + " has been removed from your custom voice channel!**");

                break;
            }

            case "addgang": {

                if (path.length < 2) {
                    Utils.sendThenDelete(channel, "`/Channel AddGang <Server Key>` - *Add all verified discord members in your gang on the specified server to this private voice channel*");
                    return;
                }

                if (!path[1].equalsIgnoreCase("gtm1") && !path[1].equalsIgnoreCase("gtm4") && !path[1].equalsIgnoreCase("gtm7")) {
                    Utils.sendThenDelete(channel, "**Unknown server key! Acceptable server keys are: `GTM1` [Minesantos], `GTM4` [Sanktburg], `GTM7` [New Mineport]**");
                    return;
                }

                Optional<CustomChannel> optionalChannel = CustomChannel.get(member);

                if (!optionalChannel.isPresent()) {
                    Utils.sendThenDelete(channel, "**You don't own a custom channel! Do `/channel create` to make one.**");
                    return;
                }

                if (gtmUser == null) {
                    Utils.sendThenDelete(channel, "**Your discord account is not linked to GTM so I can not find your gang members!**");
                    return;
                }

                ThreadUtil.runAsync( () -> {
                    try (Connection conn = BaseDatabase.getInstance(BaseDatabase.Database.USERS).getConnection()){
                        Object[] gangData = UsersDAO.getGangMembersFor(conn, gtmUser, path[1]);
                        String gangName = (String) gangData[0];
                        List<GTMUser> gangMembers = (List<GTMUser>) gangData[1];

                        if (gangMembers.size() == 0) {
                            Utils.sendThenDelete(channel, "**No gang members of `" + gangName + "` were found that are linked to the discord on Server " + path[1].toUpperCase() + "!**");
                            return;
                        }

                        // build msg and add members
                        StringBuilder sb = new StringBuilder("**Added `").append(gangName).append("` members ");
                        AtomicInteger i = new AtomicInteger();
                        gangMembers.forEach( gangMember -> {
                            i.getAndIncrement();
                            if (gangMember.getDiscordMember().isPresent()) {
                                if (gangMember == gtmUser) return; //dont add self to the channel
                                optionalChannel.get().addMember(gangMember.getDiscordMember().get());
                                sb.append(gangMember.getDiscordMember().get().getAsMention());
                                if (i.get() != gangMembers.size())
                                    sb.append(", ");
                            }
                        });
                        sb.append(" to your custom channel!**");

                        Utils.sendThenDelete(channel, sb.toString());

                    } catch (SQLException e) {
                        Utils.printStackError(e);
                    }
                });

                break;
            }

            case "reset": {

                Optional<CustomChannel> optionalChannel = CustomChannel.get(member);

                if (!optionalChannel.isPresent()) {
                    Utils.sendThenDelete(channel, "**You don't own a custom channel! Do `/channel create` to make one.**");
                    return;
                }

                optionalChannel.get().reset();
                Utils.sendThenDelete(channel, "**Resetting your custom channel " + optionalChannel.get().getChannelName() + " has been reset to default settings...!**");

                break;
            }

            case "delete": {

                Optional<CustomChannel> optionalChannel = CustomChannel.get(member);

                if (!optionalChannel.isPresent()) {
                    Utils.sendThenDelete(channel, "**You don't own a custom channel! Do `/channel create` to make one.**");
                    return;
                }

                optionalChannel.get().remove();
                Utils.sendThenDelete(channel, "**Deleting your custom channel `" + optionalChannel.get().getChannelName() + "`...!**");

                break;
            }

            case "setcategory": {

                if (!Rank.hasRolePerms(member, Rank.ADMIN)) {
                    Utils.sendThenDelete(channel, "**Sorry but only Admins+ can use this sub-command!**");
                    return;
                }

                if (path.length < 2) {
                    Utils.sendThenDelete(channel, "`/Channel SetCategory <ID>` - *Sets the selected category id as the custom channel category.*");
                    return;
                }

                long categoryId;
                try {
                    categoryId = Long.parseLong(path[1]);
                } catch (NumberFormatException e) {
                    Utils.sendThenDelete(channel, "**You provided an invalid channel id!**");
                    return;
                }

                if (guild.getCategoryById(categoryId) == null) {
                    Utils.sendThenDelete(channel, "**You provided an invalid channel id!**");
                    return;
                }

                ChannelIdData.get().setPrivateChannelsCategoryId(categoryId);
                Utils.sendThenDelete(channel, "**<@" + categoryId + "> has been successfully set as the private channels category.**");

                break;
            }

            default: {
                Utils.sendThenDelete(channel, getCommandHelp(member));
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
                .append("> `/Channel SetMax <Limit>` - *Set the maximum about of people allowed in your channel; 0 is unlimited.*\n")
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
