package commands;

import Utils.Config;
import Utils.Rank;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;

import java.util.concurrent.TimeUnit;

public class PingCommand extends Command {

    public PingCommand() {
        super("ping", "Ping the bot to see if its alive", Rank.HELPER, Type.ANYWHERE);
    }

    @Override
    public void onCommandUse(Message message, Member member, MessageChannel channel, String[] args) {
        long receiveTime = System.currentTimeMillis() - message.getTimeCreated().toInstant().toEpochMilli();

        channel.sendMessage("**Pong!** Calculating response time data...").queue((sentMsg) -> {
            long sendTime = System.currentTimeMillis() - sentMsg.getTimeCreated().toInstant().toEpochMilli();

            sentMsg.editMessage(generatePingData(receiveTime, sendTime)).queue((sentMsg2) -> {
                // Delete msgs if not dms
                if (!(channel instanceof PrivateChannel)) {
                    sentMsg.delete().queueAfter(Config.get().getDeleteTime(), TimeUnit.SECONDS);
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
