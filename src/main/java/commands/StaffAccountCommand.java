package commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import utils.MembersCache;
import utils.database.DiscordDAO;
import utils.pagination.DiscordMenu;
import utils.tools.GTools;
import utils.users.GTMUser;
import utils.users.Rank;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class StaffAccountCommand extends Command {

    public StaffAccountCommand() {
        super("accounts", "Staff command to pull gtm account information for a verified user", Rank.HELPER, Type.ANYWHERE);
    }

    @Override
    public void onCommandUse(Message message, Member member, GTMUser gtmUser, MessageChannel channel, String[] args) {

        if (args.length < 1) {
            GTools.sendThenDelete(channel, getCommandHelpMsg());
            return;
        }

        switch (args[0].toLowerCase()) {

            case "check": {

                Optional<Member> optionalTarget = MembersCache.getMember(args[1]);

                if (!optionalTarget.isPresent()) {
                    GTools.sendThenDelete(channel, "**Target user not found!**");
                    return;
                }

                GTMUser user = GTMUser.getGTMUser(optionalTarget.get().getIdLong()).orElse(null);
                if (user == null) {
                    GTools.sendThenDelete(channel, "**That user is not linked to GTM!**");
                    return;
                }

                GTools.sendThenDelete(channel, getInfoFor(member, gtmUser).build());

                break;
            }

            case "refresh": {

                Optional<Member> optionalTarget = MembersCache.getMember(args[1]);

                if (!optionalTarget.isPresent()) {
                    GTools.sendThenDelete(channel, "**Target user not found!**");
                    return;
                }

                GTMUser user = GTMUser.getGTMUser(optionalTarget.get().getIdLong()).orElse(null);
                if (user == null) {
                    GTools.sendThenDelete(channel, "**That user is not linked to GTM!**");
                    return;
                }

                GTools.runAsync(user::updateUserDataNow);
                GTools.sendThenDelete(channel, "**Successfully refreshed user data for " + optionalTarget.get().getUser().getAsTag() + " (" + gtmUser.getUsername() + ")!**");

                break;
            }

            case "refreshall": {
                if (!Rank.hasRolePerms(member, Rank.ADMIN)) {
                    GTools.sendThenDelete(channel, "**You must be an Admin or higher to use this command.**");
                    return;
                }
                channel.sendMessage("**Updating roles and data for all verified users. [Progress: `?%`]**").queue(
                        msg -> {
                            AtomicInteger index = new AtomicInteger(1);
                            List<GTMUser> users = GTMUser.loadAndGetAllUsers();

                            ScheduledFuture task = GTools.runTaskTimer( () -> {
                                int percent = Math.round((((float) index.get()) / (float) users.size()) * 100);
                                int remaining = users.size() - index.get();
                                int eta = Math.round((remaining * 710) / 1000f); // test show each user to take ~710ms to update
                                msg.editMessage("**Updating roles and data for all verified users. [Progress: `"+percent+"%`] [ETA: `" + eta + " sec`]**").complete();
                            }, 1000, 5000);
                            GTools.runAsync( () -> {
                                long start = System.currentTimeMillis();
                                for (GTMUser user : users) {
                                    user.updateUserDataNow();
                                    index.addAndGet(1);
                                }
                                task.cancel(true);
                                msg.delete().complete();
                                GTools.sendThenDelete(channel, "**All user roles and data have been successfully updated in `" + (System.currentTimeMillis() - start) + " ms`!**");
                            });
                        }
                );
                break;
            }

            case "list": {
                if (!Rank.hasRolePerms(member, Rank.ADMIN)) {
                    GTools.sendThenDelete(channel, "**You must be an Admin or higher to use this command.**");
                    return;
                }
                List<GTMUser> users = GTMUser.loadAndGetAllUsers();
                users.sort( (u1, u2) -> u1.getUsername().compareToIgnoreCase(u2.getUsername()));
                int maxPage = (int) Math.ceil(users.size() / 8f);
                DiscordMenu.create(channel, getPageInfo(users, 1, maxPage), maxPage).thenAcceptAsync( (menu -> {
                    menu.onMenuAction( (menuAction, user) -> {
                        menu.setPageContents(getPageInfo(users, menu.getPage(), menu.getMaxPages()));
                    });
                }));
                break;
            }

            default: {
                GTools.sendThenDelete(channel, getCommandHelpMsg());
                break;
            }

        }

    }

    private EmbedBuilder getInfoFor(Member member, GTMUser gtmUser) {
        return new EmbedBuilder()
                .setThumbnail(DiscordDAO.getSkullSkin(gtmUser.getUuid()))
                .setTitle("**User Profile Found!**")
                .setDescription(member.getUser().getAsTag() + "'s discord account is linked to the following GTM player...")
                .addField("**UUID:**", gtmUser.getUuid().toString(), false)
                .addField("**Username:**", "`" + gtmUser.getUsername() + "`", false)
                .addField("**Rank:**", gtmUser.getRank().n(), false)
                .setColor(new Color(207,181,59)) //gold color
                ;
    }

    private EmbedBuilder getPageInfo(List<GTMUser> gtmUserFullList, int page, int maxPage) {
        List<GTMUser> userList = new ArrayList<>(gtmUserFullList.subList((page - 1) * 8, page == maxPage ? gtmUserFullList.size() - 1 : page * 8));
        EmbedBuilder embedBuilder =  new EmbedBuilder()
                .setTitle("**Linked Users List**")
                .setColor(new Color(207,181,59)); //gold color;
        userList.forEach( (gtmUser -> {
            String name = (gtmUser.getUser().isPresent() ? gtmUser.getUser().get().getAsTag() : gtmUser.getUsername() + " `[Left Discord]`");
            embedBuilder.addField("**" + name + ":**", "`" + gtmUser.getUsername() + "`" + " (" + gtmUser.getRank().n() + ")", false);
        }));

        return embedBuilder;
    }

    private Message getCommandHelpMsg() {
        return new MessageBuilder()
                .append("> **Please enter a valid command argument:**\n")
                .append("> `/Accounts Check <Member ID / Tag>` - *Checks which player the specified discord user is linked to*\n")
                .append("> `/Accounts Refresh <Member ID / Tag>` - *Refreshes the target's information (if they are linked to GTM)*\n")
                .append("> `/Accounts RefreshAll` - *Refreshes account data for all verified users*\n")
                .append("> `/Accounts List` - *Lists all verified users*\n")
                .build();
    }

}
