package net.grandtheftmc.discordbot.commands.message;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.grandtheftmc.discordbot.commands.Command;
import net.grandtheftmc.discordbot.commands.stats.PlanServer;
import net.grandtheftmc.discordbot.utils.StringUtils;
import net.grandtheftmc.discordbot.utils.threads.ThreadUtil;
import net.grandtheftmc.discordbot.utils.users.GTMUser;
import net.grandtheftmc.discordbot.utils.users.Rank;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class ConditionalMessageCommand extends Command {

    private final HashMap<User, Set<ConditionalMessage.MessageCondition>> userConfirmationMap = new HashMap<>();

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
        Set<ConditionalMessage.MessageCondition> messageConditionSet = new HashSet<>();

        for (ConditionalOption co : ConditionalOption.values()) {
            OptionMapping om = interaction.getOption(co.getDisplay());
            if (om == null) {
                continue;
            }
            String conditionString = om.getAsString().toLowerCase().trim();

            ConditionType conditionType = null;
            PlanServer targetServer = null;
            Object value = null;

            // right now the conditionString looks something like this: ">=100:gtm1"
            for (ConditionType ct : ConditionType.values()) {
                if (conditionString.startsWith(ct.getConditionString())) {
                    conditionType = ct;
                    conditionString = conditionString.replaceFirst(ct.getConditionString(), "").trim();
                }
            }
            if (conditionType == null) {
                interaction.reply(
                        "Conditional message construction **failed**! " +
                                "Message condition type not found. Valid message conditions are:\n" +
                                "`" +
                                StringUtils.join(Arrays.stream(ConditionType.values()).map(ConditionType::getConditionString).collect(Collectors.toList()), "`, `") +
                                "`" + "\n\n" +
                                "**Example Usage** [for the last-played condition]: `>= 2022-20-04:global`\n" +
                                "This string will filter target players to everyone who last played on or after __April 20, 2022__ on ANY GTM server."
                        ).queue();
                return;
            }

            // now the conditionString looks something like this: "100:gtm1"
            String[] condStringSplit = conditionString.split(":");
            if (condStringSplit.length < 2) {
                interaction.reply(
                        "Conditional message construction **failed**! " +
                                "Invalid message formatting! Here are some examples of correctly formatted conditions:\n" +
                                "__Playtime Condition__: `<15:gtm1` - filter to players with less than 15 hrs of playtime on gtm1.\n" +
                                "__Money Condition__: `>=2500000:global` - filter to players with greater than or equal to 2.5 mil combined on all gtm servers.\n" +
                                "__Rank Condition__: `>=ELITE:gtm4` - filter to players or have a rank greater than or equal to ELITE on gtm4.\n" +
                                "__Level Condition__: `==100:global` - filter to players that have EXACTLY level 100 on ANY gtm server."
                ).queue();
                return;
            }

            for (PlanServer ps : PlanServer.values()) {
                if (condStringSplit[condStringSplit.length - 1].equals(ps.toString())) {
                    targetServer = ps;
                }
            }
            if (targetServer == null) {
                interaction.reply(
                        "Conditional message construction **failed**! " +
                                "You specified an unknown server called `" + condStringSplit[condStringSplit.length - 1] + "`!" +
                                "Known servers are:\n" +
                                StringUtils.join(Arrays.stream(PlanServer.values()).map(PlanServer::toString).collect(Collectors.toList()), "`, `")

                ).queue();
                return;
            }

            switch (om.getType()) {
                case NUMBER:
                    value = om.getAsDouble();
                    break;
                case STRING:
                    value = om.getAsString();
                    break;
                case ROLE:
                    value = om.getAsRole();
                    break;
                case USER:
                    value = om.getAsUser();
                    break;
                case BOOLEAN:
                    value = om.getAsBoolean();
                    break;
                case CHANNEL:
                    value = om.getAsGuildChannel();
                    break;
                case INTEGER:
                    value = om.getAsLong();
                    break;
                case MENTIONABLE:
                    value = om.getAsMentionable();
                    break;
                case ATTACHMENT:
                    value = om.getAsAttachment();
                    break;
                default:
                    interaction.reply("An internal **error** occured. Please check logs.").queue();
                    System.err.println("[ConditionalMessageCommand] Received an unsupported data type!");
                    return;
            }

            // need to convert into epoch time for the "last-played" condition
            if (co == ConditionalOption.LAST_PLAYED) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                assert value instanceof String;
                value = sdf.parse((String) value, new ParsePosition(0)).toInstant();
            }

            messageConditionSet.add(new ConditionalMessage.MessageCondition(co, conditionType, value, targetServer));
        }

        EmbedBuilder embedBuilders = new EmbedBuilder().setTitle("**Condition Message**");
        if (messageConditionSet.size() > 0) {
            embedBuilders.setDescription("Please review the message conditions you specified below.");
            for (ConditionalMessage.MessageCondition mc : messageConditionSet) {
                embedBuilders.addField(mc.getOption() + " (" + mc.getTargetServer() + ")", mc.getValue().toString(), false);
            }
        }
        interaction.replyEmbeds(embedBuilders.build())
                .addActionRow(Button.success("cm-confirm", "Looks Good!"), Button.danger("cm-cancel", "No! Cancel!"))
                .queue();

        userConfirmationMap.put(member.getUser(), messageConditionSet);
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        if (Objects.equals(event.getButton().getId(), "cm-confirm")) {
            Modal.Builder modalBuilder = Modal.create("cm-msg-builder", "Conditional Message Builder");

            Modal modal = modalBuilder
                    .addActionRow(
                            TextInput.create("color", "Hex-Color Code", TextInputStyle.SHORT)
                                    .setRequiredRange(6, 8)
                                    .setRequired(false)
                                    .build()
                    )
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

            event.getInteraction().replyModal(modal).queue();
        }

        else if (Objects.equals(event.getButton().getId(), "cm-cancel")) {
            event.getInteraction().editMessageEmbeds().queue();
            userConfirmationMap.remove(event.getUser());
        }

    }

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        if (event.getModalId().equals("cm-msg-builder")) {
            String title = event.getValue("title").getAsString();
            String description = event.getValue("content").getAsString();
            Color color = event.getValue("color") != null ? Color.decode(event.getValue("color").getAsString()) : Color.GREEN;
            event.deferReply().queue();

            ConditionalMessage cm = new ConditionalMessage(event.getMember(), title, description, color, userConfirmationMap.get(event.getUser()));
            cm.sendMessage();

            event.reply("Successfully sent the following conditional message to " + cm.getTargetUsers().size() + " players!").queue(
                    followUp -> followUp.sendMessageEmbeds(cm.getEmbed()).queue()
            );
        }
    }

}
