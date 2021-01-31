package events;

import utils.confighelpers.Config;
import utils.users.Rank;
import utils.console.Logs;
import utils.tools.RaidModeTools;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static utils.tools.RaidModeTools.*;

public class OnJoin extends ListenerAdapter {

    private static HashMap<Member, Long> newJoins = new HashMap<>();
    private static Member previousJoin;

    private int raidModeTime = Config.get().getRaidmodeSettings().getRaidModeTime();
    private int raidModePlayers = Config.get().getRaidmodeSettings().getRaidModePlayers();
    private int raidModePunishTime = Config.get().getRaidmodeSettings().getRaidModePunishTime();

    public void onGuildMemberJoin (GuildMemberJoinEvent e) {

        Member member = e.getMember();

        // Remove new joiners if they joined too long ago or was the same user in the calculations
        newJoins.keySet().removeIf(joined -> newJoins.get(joined) < System.currentTimeMillis() - (1000 * raidModeTime));
        newJoins.remove(member);

        // Put this newcomer in to the new joins map
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

    }


}
