package events;

import Utils.Config;
import Utils.Rank;
import Utils.tools.Logs;
import Utils.tools.RaidModeTools;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static Utils.tools.GTools.hasRolePerms;
import static Utils.tools.RaidModeTools.*;

public class OnJoin extends ListenerAdapter {

    private static HashMap<Member, Long> newJoins = new HashMap<>();
    private static Member previousJoin;

    private int raidModeTime = Config.get().getRaidModeTime();
    private int raidModePlayers = Config.get().getRaidModePlayers();
    private int raidModePunishTime = Config.get().getRaidModePunishTime();

    public void onGuildMemberJoin (GuildMemberJoinEvent e) {

        Member member = e.getMember();

        // Set the member's role to unverified
        e.getGuild().addRoleToMember(member, Rank.UNVERIFIED.er()).queue( (callback) -> {

            // Start a timer to kick user if the don't agree to rules with in 10 minutes (by checking if they still have the unverified role)
            e.getGuild().retrieveMember(e.getUser()).queueAfter(15, TimeUnit.MINUTES, (kickableMember) -> {
                if (hasRolePerms(kickableMember, Rank.UNVERIFIED)) {
                    // Note: This is also a bot prevention method that prevents bots from mass DMing members
                    kickableMember.getUser().openPrivateChannel().queue((privateChannel ->
                            privateChannel.sendMessage("**You have been kicked from the GTM Discord!** You took too react to the rules. You have to agree to the GTM Discord Rules in order to use your discord. You are free to rejoin at http://grandtheftmc.net/discord!").queue((msg) -> {
                                kickableMember.kick("Failed to agree to rules in time").queue();
                                Logs.log(member.getUser().getAsTag() + " (" + member.getId() + ") was kicked from the server because they took too long to react to the rules.");
                            })
                    ));
                }
            });

            // Remove if previous new joiners if they joined too long ago
            newJoins.keySet().removeIf(joined -> newJoins.get(joined) < System.currentTimeMillis() - (1000 * raidModeTime));

            // Put newcomer in to the new joins map
            newJoins.put(member, System.currentTimeMillis());

            // If raid mode is on and the player who just joined, joined too close to previous; kick
            if (raidMode[0] && newJoins.get(previousJoin) > System.currentTimeMillis() - (1000 * raidModePunishTime)) {

                // Kick the bots that just joined
                for (Map.Entry<Member, Long> entry2 : newJoins.entrySet()) {
                    Member bot = entry2.getKey();
                    punishBot(bot);
                }

                // Reset raid mode disable timer if automatically triggered
                if (!raidMode[1]) {
                    rescheduleDisableTask();
                }

            }

            // If at any point, there 5 or more players who join within 30 sec,
            // kick them and activate raid mode
            if (!raidMode[0]) {
                int botCount = 0;
                for (Map.Entry<Member, Long> entry : newJoins.entrySet()) {
                    Member joined = entry.getKey();
                    if (newJoins.get(joined) > System.currentTimeMillis() - (1000 * raidModeTime)) {
                        botCount++;
                    }
                    // If above threshold, server is officially being raided :)
                    if (botCount >= raidModePlayers) {
                        RaidModeTools.activateRaidMode(null);

                        // Kick the bots that just joined
                        for (Map.Entry<Member, Long> entry2 : newJoins.entrySet()) {
                            Member bot = entry2.getKey();
                            punishBot(bot);
                        }
                    }
                }
            }

            previousJoin = member;

        });

    }


}
