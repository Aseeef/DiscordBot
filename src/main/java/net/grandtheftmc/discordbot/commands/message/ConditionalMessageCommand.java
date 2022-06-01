package net.grandtheftmc.discordbot.commands.message;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
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
import net.grandtheftmc.discordbot.commands.stats.Server;
import net.grandtheftmc.discordbot.utils.StringUtils;
import net.grandtheftmc.discordbot.utils.users.GTMUser;
import net.grandtheftmc.discordbot.utils.users.Rank;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class ConditionalMessageCommand extends Command {

    private final HashMap<User, Set<ConditionalMessage.MessageCondition>> userConfirmationMap = new HashMap<>();

    public ConditionalMessageCommand() {
        super("conditionalmsg", "Conditionally private message a set of verified GTM Users.", Rank.MANAGER, Type.ANYWHERE);
    }

    @Override
    public void buildCommandData(SlashCommandData slashCommandData) {
        for (ConditionalOption co : ConditionalOption.values()) {
            slashCommandData.addOption(OptionType.STRING, co.getDisplay(), co.getDescription(), false);
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
            Server targetServer = null;
            Object value = null;

            // right now the conditionString looks something like this: ">=100:gtm1"

            // must sort by string length because we don't want something like ">" to match for a condition thats really ">="
            List<ConditionType> sortedConditions = Arrays.stream(ConditionType.values()).sorted(Comparator.comparingInt(ct -> ct.getConditionString().length())).collect(Collectors.toList());
            Collections.reverse(sortedConditions);
            for (ConditionType ct : sortedConditions) {
                if (conditionString.startsWith(ct.getConditionString())) {
                    conditionType = ct;
                    conditionString = conditionString.replaceFirst(ct.getConditionString(), "").trim();
                }
            }

            System.out.println(conditionString);

            if (conditionType == null) {
                interaction.reply(
                        "Conditional message construction **failed**! " +
                                "Message condition type not found. Valid message conditions are:\n" +
                                "`" +
                                StringUtils.join(Arrays.stream(ConditionType.values()).map(ConditionType::getConditionString).collect(Collectors.toList()), "`, `") +
                                "`" + "\n\n" +
                                "**Example Usage** [for the last-played condition]: `>= 2022-20-04:global`\n" +
                                "This string will filter target players to everyone who last played on or after __April 20, 2022__ on ANY GTM server."
                        ).setEphemeral(true).queue();
                return;
            }

            // now the conditionString looks something like this: "100:gtm1"
            String[] condStringSplit = conditionString.split(":");
            if (condStringSplit.length != 2) {
                sendInvalidFormat(interaction);
                return;
            }

            for (Server ps : Server.values()) {
                if (condStringSplit[1].equalsIgnoreCase(ps.toString())) {
                    targetServer = ps;
                }
            }
            // string must either be "GLOBAL" ORR match a server name
            if (targetServer == null && !condStringSplit[1].equalsIgnoreCase("GLOBAL")) {
                List<String> knownServers = Arrays.stream(Server.values()).filter(ps -> ps != Server.UNKNOWN).map(Server::toString).collect(Collectors.toList());
                knownServers.add("GLOBAL");

                interaction.reply(
                        "Conditional message construction **failed**! " +
                                "You specified an unknown server called `" + condStringSplit[condStringSplit.length - 1] + "`!" +
                                "Known servers are:\n" +
                                "`" + StringUtils.join(knownServers, "`, `") + "`"

                ).setEphemeral(true).queue();
                return;
            }

            condStringSplit[0] = condStringSplit[0].trim();
            try {
                if (co.getOptionType() == Double.class) {
                    value = Double.parseDouble(condStringSplit[0]);
                } else if (co.getOptionType() == Float.class) {
                    value = Float.parseFloat(condStringSplit[0]);
                } else if (co.getOptionType() == String.class) {
                    value = condStringSplit[0];
                } else if (co.getOptionType() == Boolean.class) {
                    value = Boolean.parseBoolean(condStringSplit[0]);
                } else if (co.getOptionType() == Integer.class) {
                    value = Integer.parseInt(condStringSplit[0]);
                } else if (co.getOptionType() == Long.class) {
                    value = Long.parseLong(condStringSplit[0]);
                } else {
                    interaction.reply("An internal **error** occurred. Please check logs.").setEphemeral(true).queue();
                    System.err.println("[ConditionalMessageCommand] Received an unsupported data type!");
                    return;
                }
            } catch (NumberFormatException ignored) {
                sendInvalidFormat(interaction);
                return;
            }

            // need to convert into epoch time for the "last-played" condition
            if (co == ConditionalOption.LAST_PLAYED) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                assert value instanceof String;
                value = sdf.parse((String) value, new ParsePosition(0)).toInstant();
            }

            assert value != null;
            messageConditionSet.add(new ConditionalMessage.MessageCondition(co, conditionType, value, targetServer));
        }

        EmbedBuilder embedBuilders = new EmbedBuilder().setTitle("**Condition Message**");
        if (messageConditionSet.size() > 0) {
            embedBuilders.setDescription("Please review the message conditions you specified below.");
            for (ConditionalMessage.MessageCondition mc : messageConditionSet) {
                embedBuilders
                        .addField(mc.getOption() + " (" + (mc.getTargetServer() == null ? "GLOBAL" : mc.getTargetServer()) + ")",
                                "[" + mc.getType().getConditionString() + "] " + mc.getValue().toString().toUpperCase(), false);
            }
        } else {
            embedBuilders.setDescription("No conditions were specified. Did you know, you can only message certain people by specifying conditions?")
                    .addField("Support Conditions", StringUtils.join(ConditionalOption.displayValues(), ", "), false)
                    .addField("Condition Format", "[>,>=,<,<=,=][condition_name]:[server_id/global]", false)
                    .addField("Examples",
                            "__Playtime Condition__: `<15:gtm1` - filter to players with less than 15 hrs of playtime on gtm1.\n" +
                                    "__Money Condition__: `>=2500000:global` - filter to players with greater than or equal to 2.5 mil combined on all gtm servers.\n" +
                                    "__Rank Condition__: `>=ELITE:gtm4` - filter to players or have a rank greater than or equal to ELITE on gtm4.\n" +
                                    "__Level Condition__: `=100:global` - filter to players that have EXACTLY level 100 on ANY gtm server.\n" +
                                    "__Last-Played Condition__: `>=2022-20-04:global` - filter to all players that played on or after April 20, 2022 across the network.",
                            false
                    );
        }
        interaction.replyEmbeds(embedBuilders.build())
                .addActionRow(Button.success("cm-confirm", "Looks Good!"), Button.danger("cm-cancel", "No! Cancel!"))
                .queue();

        userConfirmationMap.put(member.getUser(), messageConditionSet);
    }

    private static void sendInvalidFormat(SlashCommandInteraction interaction) {
        interaction.reply(
                "Conditional message construction **failed**! " +
                        "Invalid message formatting! Here are some examples of correctly formatted conditions:\n" +
                        "__Playtime Condition__: `<15:gtm1` - filter to players with less than 15 hrs of playtime on gtm1.\n" +
                        "__Money Condition__: `>=2500000:global` - filter to players with greater than or equal to 2.5 mil combined on all gtm servers.\n" +
                        "__Rank Condition__: `>=ELITE:gtm4` - filter to players or have a rank greater than or equal to ELITE on gtm4.\n" +
                        "__Level Condition__: `==100:global` - filter to players that have EXACTLY level 100 on ANY gtm server.\n" +
                        "__Last-Played Condition__: `>=2022-20-04:global` - filter to all players that played on or after April 20, 2022 across the network."
        ).setEphemeral(true).queue();
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        if (Objects.equals(event.getButton().getId(), "cm-confirm")) {
            Modal.Builder modalBuilder = Modal.create("cm-msg-builder", "Conditional Message Builder");

            Modal modal = modalBuilder
                    .addActionRow(
                            TextInput.create("title", "Message Title [Required]", TextInputStyle.SHORT)
                                    .setMinLength(3)
                                    .setRequired(true)
                                    .build()
                    )
                    .addActionRow(
                            TextInput.create("content", "Message Content [Required]", TextInputStyle.PARAGRAPH)
                                    .setMinLength(10)
                                    .setRequired(true)
                                    .build()
                    )
                    .addActionRow(
                            TextInput.create("color", "Hex-Color Code [Not Required]", TextInputStyle.SHORT)
                                    .setRequiredRange(6, 8)
                                    .setRequired(false)
                                    .build()
                    )
                    .addActionRow(
                            TextInput.create("image", "Message Image [Not Required]", TextInputStyle.SHORT)
                                    .setMinLength(3)
                                    .setPlaceholder("https://grandtheftmc.net/styles/ndzn/logo.png")
                                    .setRequired(false)
                                    .build()
                    )
                    .build();

            event.getInteraction().replyModal(modal).queue();
        }

        else if (Objects.equals(event.getButton().getId(), "cm-cancel")) {
            event.getInteraction().getMessage().delete().queue();
            userConfirmationMap.remove(event.getUser());
        }

    }

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        if (event.getModalId().equals("cm-msg-builder")) {
            String imageUrl = event.getValue("image") == null ? null : event.getValue("image").getAsString();
            String title = event.getValue("title").getAsString();
            String description = event.getValue("content").getAsString();
            Color color = Color.GREEN;
            try {
                color = event.getValue("color") != null ? Color.decode(event.getValue("color").getAsString()) : color;
            } catch (NumberFormatException ignored) {
            }

            event.reply("Sending messages to all target users... this might take a while...").queue();

            ConditionalMessage cm = new ConditionalMessage(event.getMember(), title, description, color, imageUrl, userConfirmationMap.get(event.getUser()));
            try {
                cm.sendMessage().thenAccept((success) -> {
                    if (success)
                        event.getHook().editOriginal("Successfully sent the following conditional message to `" + cm.getSuccessfullyMessaged() + "`/`" + cm.getTargetUsers().size() + "` of the target users!").queue(
                                followUp -> event.getHook().sendMessageEmbeds(cm.getEmbed(null)).queue()
                        );
                    else
                        event.getHook().editOriginal("An unknown error occurred when processing messages. Only `" + cm.getSuccessfullyMessaged() + "`/`" + cm.getTargetUsers().size() + "` of the target users were messaged. Please check console!").queue(
                                followUp -> event.getHook().sendMessageEmbeds(cm.getEmbed(null)).queue()
                        );
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                event.getHook().editOriginal("Something went wrong. Please check console logs. Messaged `" + cm.getSuccessfullyMessaged() + "` because running into this error.").queue();
            }

        }
    }

}
