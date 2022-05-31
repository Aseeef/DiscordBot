package selfevents;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import utils.MembersCache;
import utils.confighelpers.Config;
import utils.console.Logs;
import utils.selfdata.AnnoyData;
import utils.selfdata.ChannelData;
import utils.selfdata.ChannelIdData;
import utils.Utils;
import utils.threads.ThreadUtil;
import utils.users.GTMUser;
import utils.users.Rank;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static utils.console.Logs.log;
import static utils.Utils.*;

public class ReadyEvents extends ListenerAdapter {

    public void onReady(ReadyEvent event) {

        // Make sure bot is only on one server
        if (!inOnlyOneGuild()) {
            Logs.log("The GTM Discord bot may not be in more then one server at a time!", Logs.ERROR);
            Logs.log(JDA.getGuilds().toString(), Logs.ERROR);
            System.exit(0);
        }

        // set static guild variable
        guild = JDA.getGuilds().get(0);

        // cache all members and once done
        MembersCache.reloadMembersAsync().thenAccept( members -> {

            // load self datas
            ChannelIdData.load();
            AnnoyData.load();
            ChannelData.load();

            // Print finished loading msg
            log("Bot is now online!");

            startTasks();

            log("Player count channel updater task initialized!");

            // INVALID CONFIG OR DATA WARNINGS
            checkRaidModeSettings();
            checkSuggestionsSettings();

            // load all GTM users in to memory
            GTMUser.loadUsers();

        });

    }

    private void startTasks() {
        // Start updater repeating task to update player count
        ThreadUtil.runTaskTimer(Utils::updateOnlinePlayers, 5000, 1000L * Config.get().getMineStatSettings().getRefreshFrequency());


        // Start wisdom annoy task
        final String[] wiseQuotes = {
                "If we all work together we can make poor people come!",
                "If we all wash our hands, we can make nuclear missiles collapse under its own weight.",
                "Betrayal is in opposition to the World Health Organization.",
                "Civilization is a movie where the villain is insanity.",
                "When you're pretending to be somebody you're not, remember that you too will become an eternal flower.",
                "Aim lower. It's never too late to do it.",
                "A tattoo is all you need.",
                "Creating art can be like a fantastic commute that's hard to get around.",
                "Work in an office. Brush your teeth. Keep reminding yourself that everything happens for a reason. Ignore the inevitable.",
                "Boredom is a young woman dancing alone.",
                "Relying on an unemployed person to live like an arms dealer is pretty much as immortal as you get.",
                "How can you ensure yourself that an astronaut isn't a wife? A wife never picks up the check.",
                "Remember that you are hurting inside and remember to close your eyes when you get a turtle.",
                "Life is not a fairy tale. If you loose a shoe at midnight, you're drunk...",
                "Always be yourself. Unless you can be a unicorn in which case you should probably go ahead and be that.",
                "Use the strobe function to disorientate your attacker.",
                "Don't you think that a sperm whale can alter the way you see fear itself someday? Think about that one.",
                "If we pull ourselves together we can make stock photos mainstream! So what are you waiting for? Apply to your local McDonalds now!",
                "Always let the things you question get in the way of the things you hate.",
                "Before a donkey, comes a cake.",
                "Never let an whale tell you what to do.",
                "Understand that tomorrow is the first day of the rest of your life.",
                "If burger king burger, then is it not somebody else's foot fungus?",
                "How can mirrors be real if our eyes aren't real? Make sure to think about that one when you go to bed tonight.",
                "Between happiness and a finger is an insect.",
                "Psychology defines fearlessness as hurting oneself on purpose and still managing to surprise.",
                "It isn’t pollution that is hurting the environment, it’s the impurities in our air and water that are doing it.",
                "If a cricketer, for example, suddenly decided to go into a school and batter a lot of people to death with a cricket bat, which he could do very easily, I mean, are you going to ban cricket bats?",
                "Will the communist prevent the golden age of television? Think about that one before you eat.",
                "What criminalization that you bring when you actualize the sheep in the vicinity of the bisector?"
        };
        Timer wisdomAnnoyTask = new Timer();
        wisdomAnnoyTask.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Map<Long, Long[]> map = AnnoyData.get().getQuoteAnnoyMap();
                map.forEach( (key, value) -> {
                    long hours = value[0];
                    long lastUpdated = value[1];
                    if (lastUpdated < System.currentTimeMillis() - 1000 * 60 * 60 * hours) {
                        int index = Utils.randomNumber(0, wiseQuotes.length - 1);
                        JDA.retrieveUserById(key)
                                .flatMap(User::openPrivateChannel)
                                .flatMap(privateChannel -> privateChannel.sendMessage(wiseQuotes[index]))
                                .queue();
                        map.put(key, new Long[] {hours, System.currentTimeMillis()});
                        AnnoyData.get().save();
                    }

                });
            }
        }, 5000, 1000 * 60);
    }

    // Invalid raid mode punishment type settings
    private void checkRaidModeSettings() {
        String raidModePunishType = Config.get().getRaidmodeSettings().getRaidModePunishType();
        if (!raidModePunishType.equalsIgnoreCase("BAN") &&
                !raidModePunishType.equalsIgnoreCase("KICK") &&
                !raidModePunishType.equalsIgnoreCase("NOTIFY")) {
            log("Invalid raid mode punishment type configured in the utils.config!" +
                    " Valid punishment types are: NOTIFY, BAN, KICK", Logs.WARNING);
            log("Setting raid punishment type to \"KICK\" for now...", Logs.WARNING);
            Config.get().getRaidmodeSettings().setRaidModePunishType("KICK");
        }
    }

    // Check if suggestions channel not configured
    private void checkSuggestionsSettings() {
        if (JDA.getTextChannelById(ChannelIdData.get().getSuggestionChannelId()) == null)
            log("The suggestion channel has not been properly configured yet!",
                    Logs.WARNING);
        // Player count channel not configured
        if (JDA.getVoiceChannelById(ChannelIdData.get().getPlayerCountChannelId()) == null)
            log("The player count display channel has not been properly configured yet!",
                    Logs.WARNING);
        // Raid alerts channel not configured
        if (JDA.getTextChannelById(ChannelIdData.get().getRaidAlertChannelId()) == null)
            log("The raid alerts channel has not been properly configured yet!",
                    Logs.WARNING);
    }

    private static boolean inOnlyOneGuild() {
        return JDA.getGuilds().size() <= 1;
    }

}