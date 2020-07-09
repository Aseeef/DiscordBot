package utils;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import utils.console.Logs;
import utils.users.GTMUser;
import utils.users.Rank;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static utils.tools.GTools.guild;

public class MembersCache extends ListenerAdapter {

    private static List<Member> members = new ArrayList<>();

    public void onGuildMemberJoin (GuildMemberJoinEvent event) {
        members.add(event.getMember());
    }

    public void onGuildMemberRemove (GuildMemberRemoveEvent event) {
        members.remove(event.getMember());
    }

    public static List<Member> getMembers() {
        return members;
    }

    public static List<Member> getMembersWithRolePerms (Rank rank) {
        return members.stream().filter( member -> Rank.hasRolePerms(member, rank)).collect(Collectors.toList());
    }

    public static boolean doesMemberExist(long id) {
        return members.stream().anyMatch( member -> member.getIdLong() == id);
    }

    public static Optional<Member> getMember(long id) {
        return members.stream().filter( member -> member.getIdLong() == id).findFirst();
    }

    public static Optional<Member> getMemberFromTag(String tag) {
        return members.stream().filter( member -> member.getUser().getAsTag().equalsIgnoreCase(tag)).findFirst();
    }

    public static Optional<Member> getMemberFromMention(String mention) {
        return members.stream().filter( member -> member.getAsMention().equalsIgnoreCase(mention)).findFirst();
    }

    /**
     * Tries to get member through multiple methods including checking id, mention, tag, ign
     */
    public static Optional<Member> getMember(String s) {
        Optional<Member> optionalMember;

        optionalMember = getMemberFromMention(s);
        if (optionalMember.isPresent()) return optionalMember;

        optionalMember = getMemberFromTag(s);
        if (optionalMember.isPresent()) return optionalMember;
        // try get member
        try {
            optionalMember = getMember(Long.parseLong(s));
            if (optionalMember.isPresent()) return optionalMember;
        } catch (NumberFormatException ignored) {
        }

        Optional<GTMUser> optionalGTMUser = GTMUser.loadAndGetAllUsers().stream().filter( gtmUser -> gtmUser.getUsername().equalsIgnoreCase(s)).findFirst();
        if (optionalGTMUser.isPresent()) return optionalGTMUser.get().getDiscordMember();

        return Optional.empty();
    }

    public static Optional<User> getUser(long id) {
        return getMember(id).map((Member::getUser));
    }

    public static void reloadMembersAsync() {
        long cacheStart = System.currentTimeMillis();
        guild.loadMembers().onSuccess( (list) -> {
            members.clear();
            members.addAll(list);
            Logs.log("Successfully cached all members in " + (System.currentTimeMillis() - cacheStart) + " ms!");
        });
    }

    public static void reloadMembers() {
        long cacheStart = System.currentTimeMillis();
        members.clear();
        members.addAll(guild.loadMembers().get());
        Logs.log("Successfully cached all members in " + (System.currentTimeMillis() - cacheStart) + " ms!");
    }

}
