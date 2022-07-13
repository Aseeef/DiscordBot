package net.grandtheftmc.discordbot.utils.database.redis;

import net.grandtheftmc.discordbot.GTMBot;
import net.grandtheftmc.discordbot.utils.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.grandtheftmc.discordbot.utils.selfdata.ChannelIdData;
import net.grandtheftmc.simplejedis.JedisEventListener;
import org.json.JSONException;
import org.json.JSONObject;
import net.grandtheftmc.discordbot.utils.MembersCache;
import net.grandtheftmc.discordbot.utils.console.Logs;
import net.grandtheftmc.discordbot.utils.threads.ThreadUtil;
import net.grandtheftmc.discordbot.utils.tools.Verification;
import net.grandtheftmc.discordbot.utils.users.GTMUser;
import net.grandtheftmc.discordbot.utils.users.Rank;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class OnRecieveMessageGTM implements JedisEventListener {

    @Override
    public String[] getChannels() {
        return new String[] {"gtm_to_discord"};
    }

    @Override
    public void onJedisEvent(String channel, String senderId, CompletableFuture<Object> callback, JSONObject jsonObject) {
        try {

            String action = jsonObject.getString("action");

            Logs.log("[DEBUG] [OnRedisMessageReceive] Received the following data from type " + action + " from GTM!");
            Logs.log("[DEBUG] [OnRedisMessageReceive] " + jsonObject);

            switch (action.toLowerCase()) {

                case "inactive_player": {
                    // TODO: Message players offering them gift cards to tell us why they stopped playing!!?
                }

                case "verify": {
                    UUID uuid = UUID.fromString(jsonObject.getString("uuid"));
                    String name = jsonObject.getString("name");
                    Rank rank = Rank.getRankFromString(jsonObject.getString("rank"));
                    String verifyCode = jsonObject.getString("code");
                    if (rank == null) return;
                    Verification.createVerifyRequest(verifyCode, uuid, name, rank);
                    break;
                }

                case "unverify": {
                    long discordId = jsonObject.getLong("discordId");
                    GTMUser.removeGTMUser(discordId);
                    break;
                }

                case "update": {
                    long discordId = jsonObject.getLong("discordId");
                    GTMUser.getGTMUser(discordId).ifPresent(((user) -> ThreadUtil.runAsync(user::updateUserDataNow)));
                    break;
                }

                case "staffvlreport": {
                    String player = jsonObject.getString("player");
                    String check = jsonObject.getString("check");
                    int intensity = jsonObject.getInt("intensity");
                    int ping = jsonObject.getInt("ping");
                    double tps = jsonObject.getDouble("tps");
                    String component = jsonObject.getString("component");
                    String server = jsonObject.getString("server");

                    if (intensity < 3) return; //TODO: Update in bungee

                    List<Member> managers = MembersCache.getMembersWithRolePerms(Rank.MANAGER);
                    for (Member manager : managers) {
                        manager.getUser().openPrivateChannel().queue((privateChannel) -> {
                            privateChannel.sendMessageEmbeds(generateStaffReportEmbed(player, check, ping, tps, component, server)).queue();
                        });
                    }

                    break;
                }

                case "dupe": {
                    String name = jsonObject.getString("duper-name");
                    UUID uuid = UUID.fromString(jsonObject.getString("duper-uuid"));
                    long detectionTime = jsonObject.getLong("detection-time");
                    String dupeType = jsonObject.getString("dupe-type");

                    DupeAlert da = new DupeAlert(name, uuid, detectionTime, dupeType);

                    // currently the only dupe type
                    if (dupeType.equalsIgnoreCase("WELL-COIN")) {
                        int coinId = jsonObject.getInt("coin-id");
                        UUID coinCreator = UUID.fromString(jsonObject.getString("coin-creator"));
                        UUID usedBy = jsonObject.has("used-by") && jsonObject.getString("used-by") != null ? UUID.fromString(jsonObject.getString("used-by")) : null;
                        GTMBot.getJDA().getTextChannelById(ChannelIdData.get().getAdminChannelId())
                                .sendMessageEmbeds(generateWellDupeEmbed(da, coinId, coinCreator, usedBy)).queue();
                    }

                    break;
                }

                default:
                    Logs.log("[DEBUG] [OnRedisMessageReceive] '" + action + "' is an unknown action!", Logs.WARNING);
                    break;


            }
        } catch (JSONException e) {
            Utils.printStackError(e);
        }

    }

    private MessageEmbed generateStaffReportEmbed(String player, String check, int ping, double tps, String component, String server) {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
        sdf.setTimeZone(TimeZone.getTimeZone("America/New_York"));
        String timestamp = sdf.format(new Date(System.currentTimeMillis()));
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("Gary Anticheat Notification")
                .setColor(Color.RED)
                .setDescription("Gary has just detected that the staff member **" + player + "** might be using cheats on GTM. For your information details on these detects are as follows:")
                .addField("**Timestamp:**", timestamp, true)
                .addField("**Check Failed:**", check, true)
                .addField("**Component Failed:**", component, true)
                .addField("**Ping:**", String.valueOf(ping), true)
                .addField("**TPS:**", String.valueOf(tps), true)
                .addField("**Server**", server, true);

        return embed.build();
    }

    private MessageEmbed generateWellDupeEmbed(DupeAlert da, int coinId, UUID coinCreator, UUID usedBy) {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
        sdf.setTimeZone(TimeZone.getTimeZone("America/New_York"));
        String timestamp = sdf.format(new Date(da.getDetectionTime()));
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("**DUPE ALERT!**")
                .setColor(Color.BLUE)
                .setDescription("A player just tried to use a **duplicated wishing well coin**! Details are as follows:")
                .addField("**Duper Username:**", da.getName(), true)
                .addField("**Duper UUID:**", da.getDuperUUID().toString(), true)
                .addField("**Timestamp:**", timestamp, true)
                .addField("**Duped Coin ID:**", String.valueOf(coinId), true)
                .addField("**Coin Creator:**", coinCreator.toString(), true)
                .addField("**Originally Used By:**", usedBy == null ? "N/A" : usedBy.toString(), true);

        return embed.build();
    }

}
