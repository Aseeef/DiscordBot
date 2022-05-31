package net.grandtheftmc.discordbot.commands;

import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.grandtheftmc.discordbot.GTMBot;
import net.grandtheftmc.discordbot.utils.Utils;
import net.grandtheftmc.discordbot.utils.channels.CustomChannel;
import net.grandtheftmc.discordbot.utils.database.UsersDAO;
import net.grandtheftmc.discordbot.utils.database.sql.BaseDatabase;
import net.grandtheftmc.discordbot.utils.selfdata.ChannelIdData;
import net.grandtheftmc.discordbot.utils.threads.ThreadUtil;
import net.grandtheftmc.discordbot.utils.users.GTMUser;
import net.grandtheftmc.discordbot.utils.users.Rank;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class ChannelCommand extends Command {

    public ChannelCommand() {
        super("channel", "Create and manage your own private voice channels", Rank.VIP, Type.DISCORD_ONLY);
    }

    @Override
    /*
    .append("> **Please enter a valid command argument:**\n")
                .append("> `/Channel Create (name)` - **\n")
                //.append("> `/Channel Rename <name>` - *Rename your custom channel to the given argument.*\n")
                .append("> `/Channel SetPublic <True/False>` - **\n")
                .append("> `/Channel SetMax <Limit>` - **\n")
                .append("> `/Channel Add <Member ID / Tag>` - **\n")
                .append("> `/Channel Remove <Member ID / Tag>` - **\n")
                .append("> `/Channel AddGang <Server Key>` - *Add all verified discord members in your gang on the specified server to this private voice channel*\n")
                .append("> `/Channel Reset` - **\n")
                .append("> `/Channel Delete` - **\n");
                if (Rank.hasRolePerms(member, Rank.ADMIN)) {
                    mb.append("> `/Channel SetCategory <ID>` - **\n");
                }
     */
    public void buildCommandData(SlashCommandData slashCommandData) {
        SubcommandData create = new SubcommandData("create", "Create a new custom channel with the selected name.");
        create.addOption(OptionType.STRING, "channel-name", "The name that you want to give to your channel", false);

        SubcommandData setPublic = new SubcommandData("setpublic", "Configure whether everyone should be able to join your channel.");
        setPublic.addOption(OptionType.BOOLEAN, "public", "Should this be a public channel? (true=yes, false=no)", true);

        SubcommandData setMax = new SubcommandData("setmax", "Set the maximum about of people allowed in your channel; 0 is unlimited.");
        setMax.addOption(OptionType.INTEGER, "max-users", "The maximum people allowed in your channel at anytime.", true);

        SubcommandData add = new SubcommandData("add", "Add the selected discord member to your custom channel.");
        add.addOption(OptionType.USER, "user", "The user you want to add or whitelist to your channel", true);

        SubcommandData addGang = new SubcommandData("addgang", "Add all verified discord members in your gang on the specified server to this private voice channel");
        OptionData gangOption = new OptionData(OptionType.STRING, "server", "Which GTM server is your gang on?", true);
        gangOption.addChoice("gtm1", "gtm1");
        gangOption.addChoice("gtm4", "gtm4");
        addGang.addOptions(gangOption);

        SubcommandData remove = new SubcommandData("remove", "Remove the selected discord member from your custom channel.");
        remove.addOption(OptionType.USER, "user", "The user you want to remove or blacklist from your channel", true);

        SubcommandData reset = new SubcommandData("reset", "Reset your custom channel to default removing all whitelisted and blacklisted users.");

        SubcommandData delete = new SubcommandData("delete", "Delete your custom channel.");

        SubcommandData setCategory = new SubcommandData("setcategory", "Sets the selected category id as the custom channel category.");
        OptionData optionData = new OptionData(OptionType.CHANNEL, "category", "The category id for the category to be used for custom channels", true);
        optionData.setChannelTypes(ChannelType.CATEGORY);
        setCategory.addOptions(optionData);

        slashCommandData.addSubcommands(create, setPublic, setMax, add, addGang, remove, reset, delete, setCategory);
    }

    @Override
    public void onCommandUse(SlashCommandInteraction interaction, MessageChannel channel, List<OptionMapping> arguments, Member member, GTMUser gtmUser, String[] path) {

        // only sender sees replies
        interaction.getHook().setEphemeral(true);

        switch (path[0].toLowerCase()) {

            case "create": {

                if (!CustomChannel.canCreateChannels()) {
                    interaction.reply("**The custom channel category is not configured yet! Please ask an admin to configure this.**").queue();
                    return;
                }

                if (CustomChannel.get(member).isPresent()) {
                    interaction.reply("**You can only have `1` private channel at a time!**").setEphemeral(true).queue();
                    return;
                }

                String name;
                if (interaction.getOption("channel-name") == null)
                    name = member.getEffectiveName() + "'s Private Channel";
                else name = interaction.getOption("channel-name").getAsString();

                CustomChannel customChannel = new CustomChannel(name, member, false);
                customChannel.create().thenAccept( (vc) -> {
                    String msg = "**Successfully created your private channel `" + vc.getName() + "`!**\n";
                    msg += customChannel.getInvite().getUrl();
                    interaction.reply(msg).setEphemeral(true).queue();
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

                Optional<CustomChannel> optionalChannel = CustomChannel.get(member);

                if (!optionalChannel.isPresent()) {
                    interaction.reply("**You don't own a custom channel! Do `/channel create` to make one.**").setEphemeral(true).queue();
                    return;
                }

                boolean publicChannel = interaction.getOption("public").getAsBoolean();
                optionalChannel.get().setPublicChannel(publicChannel);
                interaction.reply("**Setting your custom channel to " + (publicChannel ? "public" : "private") + "!**").setEphemeral(true).queue();

                break;
            }

            case "setmax": {

                Optional<CustomChannel> optionalChannel = CustomChannel.get(member);

                if (!optionalChannel.isPresent()) {
                    interaction.reply("**You don't own a custom channel! Do `/channel create` to make one.**").setEphemeral(true).queue();
                    return;
                }

                int limit = interaction.getOption("max-users").getAsInt();

                optionalChannel.get().setChannelMax(limit);
                interaction.reply("**Setting max channel user limit to " + limit + " users...!**").setEphemeral(true).queue();

                break;
            }

            case "add": {

                Optional<CustomChannel> optionalChannel = CustomChannel.get(member);

                if (!optionalChannel.isPresent()) {
                    interaction.reply("**You don't own a custom channel! Do `/channel create` to make one.**").setEphemeral(true).queue();
                    return;
                }

                Member target = interaction.getOption("user").getAsMember();

                if (target.equals(member)) {
                    interaction.reply("**You can't add yourself to your channel because you own it!**").setEphemeral(true).queue();
                    return;
                }

                optionalChannel.get().addMember(target);
                interaction.reply("**Inviting " + target.getAsMention() + " to your custom channel!**").setEphemeral(true).queue();

                break;
            }

            case "remove": {

                Optional<CustomChannel> optionalChannel = CustomChannel.get(member);

                if (!optionalChannel.isPresent()) {
                    interaction.reply("**You don't own a custom channel! Do `/channel create` to make one.**").setEphemeral(true).queue();
                    return;
                }

                Member target = interaction.getOption("user").getAsMember();

                if (target.equals(member)) {
                    interaction.reply("**You can't remove yourself from your own channel!**").setEphemeral(true).queue();
                    return;
                }

                if (Rank.hasRolePerms(target, Rank.ADMIN)) {
                    interaction.reply("**Sorry but it is not possible to block admins from your custom channel!**").setEphemeral(true).queue();
                    return;
                }

                optionalChannel.get().removeMember(target);

                if (target.getVoiceState() != null && target.getVoiceState().inAudioChannel() &&
                        target.getVoiceState().getChannel().getIdLong() == optionalChannel.get().getVoiceChannel().getIdLong()) {
                    GTMBot.getGTMGuild().kickVoiceMember(target).queue();
                }

                interaction.reply("**" + target.getAsMention() + " has been removed from your custom voice channel!**").setEphemeral(true).queue();
                break;
            }

            case "addgang": {

                String server = interaction.getOption("server").getAsString();

                Optional<CustomChannel> optionalChannel = CustomChannel.get(member);

                if (!optionalChannel.isPresent()) {
                    interaction.reply("**You don't own a custom channel! Do `/channel create` to make one.**").setEphemeral(true).queue();
                    return;
                }

                if (gtmUser == null) {
                    interaction.reply("**Your discord account is not linked to GTM so I can not find your gang members!**").setEphemeral(true).queue();
                    return;
                }

                ThreadUtil.runAsync( () -> {
                    try (Connection conn = BaseDatabase.getInstance(BaseDatabase.Database.USERS).getConnection()){

                        Object[] gangData = UsersDAO.getGangMembersFor(conn, gtmUser, server);
                        String gangName = (String) gangData[0];
                        List<GTMUser> gangMembers = (List<GTMUser>) gangData[1];

                        if (gangName == null) {
                            interaction.reply("**You are not in a gang on `" + server + "`!**").setEphemeral(true).queue();
                            return;
                        }

                        if (gangMembers.size() == 0) {
                            interaction.reply("**No gang members of `" + gangName + "` were found that are linked to the discord on Server " + server.toUpperCase() + "!**").setEphemeral(true).queue();
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

                        interaction.reply(sb.toString()).setEphemeral(true).queue();

                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                });

                break;
            }

            case "reset": {

                Optional<CustomChannel> optionalChannel = CustomChannel.get(member);

                if (!optionalChannel.isPresent()) {
                    interaction.reply("**You don't own a custom channel! Do `/channel create` to make one.**").setEphemeral(true).queue();
                    return;
                }

                optionalChannel.get().reset();
                interaction.reply("**Resetting your custom channel " + optionalChannel.get().getChannelName() + " has been reset to default settings...!**").setEphemeral(true).queue();

                break;
            }

            case "delete": {

                Optional<CustomChannel> optionalChannel = CustomChannel.get(member);

                if (!optionalChannel.isPresent()) {
                    interaction.reply("**You don't own a custom channel! Do `/channel create` to make one.**").setEphemeral(true).queue();
                    return;
                }

                optionalChannel.get().remove();
                interaction.reply("**Deleting your custom channel `" + optionalChannel.get().getChannelName() + "`...!**").setEphemeral(true).queue();

                break;
            }

            case "setcategory": {

                if (!Rank.hasRolePerms(member, Rank.ADMIN)) {
                    interaction.reply("**Sorry but only Admins+ can use this sub-command!**").setEphemeral(true).queue();
                    return;
                }

                long categoryId = interaction.getOption("category").getAsGuildChannel().getIdLong();

                ChannelIdData.get().setPrivateChannelsCategoryId(categoryId);
                interaction.reply("**<@" + categoryId + "> has been successfully set as the private channels category.**").setEphemeral(true).queue();

                break;
            }

            default: {
                throw new IllegalArgumentException();
            }

        }

    }

}
