package net.grandtheftmc.discordbot.events;

import net.grandtheftmc.discordbot.GTMBot;
import net.grandtheftmc.discordbot.utils.confighelpers.Config;
import net.grandtheftmc.discordbot.utils.tools.RaidModeTools;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.grandtheftmc.discordbot.utils.tools.WelcomeTools;

import java.util.HashMap;
import java.util.Map;

import static net.grandtheftmc.discordbot.utils.tools.RaidModeTools.*;

public class OnJoin extends ListenerAdapter {

    private static final HashMap<Member, Long> newJoins = new HashMap<>();
    private static Member previousJoin;

    private final int raidModeTime = Config.get().getRaidmodeSettings().getRaidModeTime();
    private final int raidModePlayers = Config.get().getRaidmodeSettings().getRaidModePlayers();
    private final int raidModePunishTime = Config.get().getRaidmodeSettings().getRaidModePunishTime();

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

        // if they are still a member after all that, message them a welcome message
        if (GTMBot.getGTMGuild().isMember(member.getUser())) {
            member.getUser().openPrivateChannel().queue(pc -> {
                pc.sendMessageEmbeds(WelcomeTools.getWelcomeEmbed(member.getUser())).queue();
            });
        }

    }


}
