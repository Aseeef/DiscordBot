package net.grandtheftmc.discordbot.utils.tools;

import net.grandtheftmc.discordbot.GTMBot;
import net.grandtheftmc.discordbot.utils.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;

import javax.net.ssl.HttpsURLConnection;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

import static net.grandtheftmc.discordbot.utils.console.Logs.log;

public class WelcomeTools {

    public static MessageEmbed getRuleReactEmbed() {

        EmbedBuilder embed = new EmbedBuilder();
        String gtmCheeky = GTMBot.getJDA().getEmojisByName("gtmcheeky", true).get(0).getAsMention();
        String gtmAgree = GTMBot.getJDA().getEmojisByName("gtmagree", true).get(0).getAsMention();
        String gtmDisagree = GTMBot.getJDA().getEmojisByName("gtmdisagree", true).get(0).getAsMention();

        embed.setTitle("Welcome to the Grand Theft Minecart Discord!");
        embed.setDescription(
                "Hey there! Before we can get started, **you must read and agree to our rules** by reacting below with a " + gtmAgree + "! You can also react with a " + gtmDisagree + " but then you know... you can't use the discord " + gtmCheeky + "." +
                "\n\n" + "**DISCORD RULES:** https://grandtheftmc.net/threads/discord-rules.10052/#post-36895"
        );
        embed.setColor(new Color(133, 186, 101));
        embed.setFooter("Simply click one of the below reactions to respond!", GTMBot.getJDA().getSelfUser().getAvatarUrl());

        return embed.build();
    }

    public static MessageEmbed getWelcomeEmbed(User user) {

        EmbedBuilder embed = new EmbedBuilder();

        embed.setTitle("Welcome to the Grand Theft Minecart Discord!");
        embed.setDescription(
                "Now that we are through the formalities, " + user.getAsMention() + ", you are now officially welcome to the **GTM discord**! We are happy to have you! Here on the discord, you can stay connected to the **latest announcements** from us, stay up to date with any **giveaways**, **get support** from our dedicated staff team,  **connect with awesome new people** from around the world, and oh **so much more**! Feel free to explore around the discord. See you around \uD83D\uDE0A!" +
                        "\n\n\n" + "__**Useful Server Links**__" + "\n" +
                        "\uD83D\uDD30 **Server IP** » gtm.network\n" +
                        "\uD83D\uDD30 **Website** » https://grandtheftmc.net/\n" +
                        "\uD83D\uDD30 **Store** » https://store.grandtheftmc.net/\n" +
                        "\uD83D\uDD30 **Wiki** » https://wiki.grandtheftmc.net/\n" +
                        "\uD83D\uDD30 **Web Map** » https://grandtheftmc.net/map/\n" +
                        "\uD83D\uDD30 **Support Tickets** » https://grandtheftmc.net/appeal/"
        );
        embed.setColor(new Color(133, 186, 101));
        embed.setFooter("Grand Theft Minecart", GTMBot.getJDA().getSelfUser().getAvatarUrl());

        return embed.build();
    }

    private static String getFieldContent(ArrayList<String> lines, int i) {

        int fieldSize = getFieldSize(lines, i);
        int offset = i + 1;

        StringBuilder sb = new StringBuilder();
        for (int j = offset ; j < fieldSize + offset ; j++) {
            sb.append(lines.get(j)).append("\n");
        }

        return sb.toString();
    }

    private static int getFieldSize(ArrayList<String> lines, int i) {
        int fieldSize = 0;
        int offset = i + 1;

        for (int j = offset ; j < lines.size() ; j++) {
            if (!lines.get(j).startsWith("|")) {
                fieldSize++;
            }
            else break;
        }
        return fieldSize;
    }

    public static ArrayList<String> getPost() {

        ArrayList<String> lines = new ArrayList<>();

        try {
            //Instantiating the URL class
            String httpsURL = "https://grandtheftmc.net/threads/gtm-rules-and-regulations.10052/";

            URL myurl = new URL(httpsURL);
            HttpsURLConnection con = (HttpsURLConnection) myurl.openConnection();
            con.setRequestProperty ( "User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:63.0) Gecko/20100101 Firefox/63.0" );
            InputStream ins = con.getInputStream();

            //Retrieving the contents of the specified page
            BufferedReader br = new BufferedReader(new InputStreamReader(ins));

            boolean discordRules = false;

            String line;
            while ((line = br.readLine()) != null) {

                if (line.contains("This section will include all the Discord rules.")) {
                    discordRules = true;
                    continue;
                } else if (line.contains("The above mentioned Discord server rules are a community guideline.")) {
                    break;
                }

                if (line.contains("----------"))
                    continue;

                if (discordRules) {
                    line = line.replaceAll("<[^>]*>", "")
                            .replaceAll("&[^;]*;", "")
                            .replaceAll("\n+", "\n");
                    if (line.startsWith("-") || line.startsWith("|")) {
                        lines.add(line);
                    }
                }

            }

            for (String l : lines)
                log(l);

        } catch (IOException e) {
            Utils.printStackError(e);
        }

        return lines;

    }

}
