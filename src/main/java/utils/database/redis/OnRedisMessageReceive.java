package utils.database.redis;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.grandtheftmc.jedisnew.RedisEventListener;
import org.json.JSONException;
import org.json.JSONObject;
import utils.Rank;
import utils.console.Logs;
import utils.database.DiscordDAO;
import utils.database.sql.BaseDatabase;
import utils.tools.GTools;
import utils.tools.Verification;
import utils.users.GTMUser;

import java.awt.*;
import java.sql.Connection;
import java.sql.SQLException;
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
                        GTMUser.getGTMUser(discordId).ifPresent((GTMUser::updateUserDataNow));
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
                        try (Connection conn = BaseDatabase.getInstance(BaseDatabase.Database.USERS).getConnection()) {
                            List<GTMUser> managers = DiscordDAO.getAllWithRank(conn, Rank.MANAGER);
                            managers.forEach( (gtmUser ->
                                    gtmUser.getDiscordMember().getUser().openPrivateChannel().queue( (privateChannel ->
                                    privateChannel.sendMessage(generateStaffReportEmbed(player, check, ping, tps, component, server)).queue()
                                            ))));
                        } catch (SQLException e) {
                            GTools.printStackError(e);
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
