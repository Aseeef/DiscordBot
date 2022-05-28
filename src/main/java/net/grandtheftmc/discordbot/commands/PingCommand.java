package net.grandtheftmc.discordbot.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.grandtheftmc.discordbot.utils.confighelpers.Config;
import net.grandtheftmc.discordbot.utils.users.GTMUser;

import java.util.concurrent.TimeUnit;

public class PingCommand extends Command {

    public PingCommand() {
        super("ping", "Ping the bot to see if its alive", null, Type.ANYWHERE);
    }

    @Override
    public void onCommandUse(SlashCommandInteraction interaction, MessageChannel channel, Member member, GTMUser gtmUser, String[] args) {
        long receiveTime = System.currentTimeMillis() - interaction.getTimeCreated().toInstant().toEpochMilli();

        channel.sendMessage("**Pong!** Calculating response time data...").queue((sentMsg) -> {
            long sendTime = System.currentTimeMillis() - sentMsg.getTimeCreated().toInstant().toEpochMilli();

            sentMsg.editMessageEmbeds(generatePingData(receiveTime, sendTime)).queue((sentMsg2) -> {
                // Delete msgs if not dms
                if (!(channel instanceof PrivateChannel)) {
                    sentMsg.delete().queueAfter(Config.get().getMsgDeleteTime(), TimeUnit.SECONDS);
                }
            });
        });
    }

    private MessageEmbed generatePingData(long receivedTime, long sendTime) {

        String statusMsg = receivedTime < 250 ?
                "This is a normal response time. The bot is functioning fine!" : "The bot took too long to respond. Something is not right...";

        return new EmbedBuilder()
                .setTitle("**Bot Response Statistics**")
                .setDescription(statusMsg)
                .addField("Receive Time:", receivedTime + " ms",true)
                .addField("Send Time:", sendTime + " ms", true)
                .addField("Total Reply Time:", (receivedTime + sendTime) + " ms", true)
                .build();
    }

}
