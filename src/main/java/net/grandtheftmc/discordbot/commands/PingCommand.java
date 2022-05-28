package net.grandtheftmc.discordbot.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.grandtheftmc.discordbot.utils.users.GTMUser;

import java.util.List;

public class PingCommand extends Command {

    public PingCommand() {
        super("ping", "Ping the bot to see if its alive", null, Type.ANYWHERE);
    }

    @Override
    public void buildCommandData(SlashCommandData slashCommandData) {
        // no command args needed
    }

    @Override
    public void onCommandUse(SlashCommandInteraction interaction, MessageChannel channel, List<OptionMapping> arguments, Member member, GTMUser gtmUser, String[] path) {
        long receiveTime = System.currentTimeMillis() - interaction.getTimeCreated().toInstant().toEpochMilli();

        interaction.reply("**Pong!** Calculating response time data...").queue((sentMsg) -> {
            long sendTime = System.currentTimeMillis() - sentMsg.getInteraction().getTimeCreated().toInstant().toEpochMilli();
            interaction.getHook().editOriginalEmbeds(generatePingData(receiveTime, sendTime)).queue();
        });

    }

    private MessageEmbed generatePingData(long receivedTime, long sendTime) {

        String statusMsg = receivedTime < 200 ?
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
