package net.grandtheftmc.discordbot.commands.message;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.grandtheftmc.discordbot.commands.Command;
import net.grandtheftmc.discordbot.utils.users.GTMUser;
import net.grandtheftmc.discordbot.utils.users.Rank;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class ConditionalMessageCommand extends Command {

    public ConditionalMessageCommand() {
        super("conditionalmsg", "Conditionally private message a set of verified GTM Users.", Rank.MANAGER, Type.ANYWHERE);
    }

    @Override
    public void buildCommandData(SlashCommandData slashCommandData) {
        for (ConditionalOption co : ConditionalOption.values()) {
            slashCommandData.addOption(co.getOptionType(), co.getDisplay(), co.getDescription(), false);
        }
    }



    @Override
    public void onCommandUse(SlashCommandInteraction interaction, MessageChannel channel, List<OptionMapping> arguments, Member member, GTMUser gtmUser, String[] path) {
        Modal.Builder modalBuilder = Modal.create("message-builder", "Conditional Message Builder");
        //for (MessageCondition mc : MessageCondition.values()) {
        //    modalBuilder.addActionRow(
        //                TextInput.create(mc.name().toLowerCase(), mc.name(), TextInputStyle.SHORT).build()
        //        );
        //}
        Modal modal = modalBuilder
                .addActionRow(
                        TextInput.create("title", "Message Title", TextInputStyle.SHORT)
                                .setMinLength(3)
                                .setRequired(true)
                                .build()
                )
                .addActionRow(
                        TextInput.create("content", "Message Content", TextInputStyle.PARAGRAPH)
                                .setMinLength(10)
                                .setRequired(true)
                                .build()
                )
                .build();
        interaction.replyModal(modal).queue();
    }

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        String[] conditions = event.getValue("conditions").getAsString().split(" *, *");
        System.out.println(Arrays.toString(conditions));
        for (String conditionString : conditions) {

        }

        event.reply("Success!").queue();
    }

}
