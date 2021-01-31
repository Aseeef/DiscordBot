package utils.database.redis;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.grandtheftmc.jedisnew.RedisEventListener;
import org.json.JSONException;
import org.json.JSONObject;
import utils.MembersCache;
import utils.console.Logs;
import utils.tools.GTools;
import utils.tools.Verification;
import utils.users.GTMUser;
import utils.users.Rank;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

public class OnRedisMessageReceive implements RedisEventListener {

    @Override
    public String getChannel() {
        return "gtm_to_discord";
    }

    @Override
    public void onRedisEvent(String s, JSONObject jsonObject) {
        GTools.runAsync( () -> {

            try {

                String action = jsonObject.getString("action");

                Logs.log("[DEBUG] [OnRedisMessageReceive] Received the following data from type " + action + " from GTM!");
                Logs.log("[DEBUG] [OnRedisMessageReceive] " + jsonObject.toString());

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
                        GTMUser.getGTMUser(discordId).ifPresent(( (user) -> GTools.runAsync(user::updateUserDataNow)));
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
                                privateChannel.sendMessage(generateStaffReportEmbed(player, check, ping, tps, component, server)).queue();
                            });
                        }

                        break;
                    }

                    default:
                        Logs.log("[DEBUG] [OnRedisMessageReceive] '" + action + "' is an unknown action!", Logs.WARNING);
                        break;


                }
            } catch (JSONException e) {
                GTools.printStackError(e);
            }

        });

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

}
