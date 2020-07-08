package utils.channels;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.requests.restaction.PermissionOverrideAction;
import utils.SelfData;

import java.util.*;

import static utils.tools.GTools.guild;

public class CustomChannel {

    private static Map<Member, CustomChannel> channelMap = new HashMap<>();

    private String channelName;
    private Member owner;
    private List<Member> whitelist;
    private boolean isPublicChannel;
    private VoiceChannel voiceChannel;

    public CustomChannel(String channelName, Member owner, boolean isPublicChannel) {
        this.channelName = channelName;
        this.owner = owner;
        this.isPublicChannel = isPublicChannel;

        channelMap.put(owner, this);
    }

    public static Optional<CustomChannel> get(Member owner) {
        return Optional.ofNullable(channelMap.getOrDefault(owner, null));
    }

    public void create() {
        SelfData.get().getPrivateChannelsCategory().createVoiceChannel(this.channelName)
        .queue( vc -> {
            this.voiceChannel = vc;
            vc.upsertPermissionOverride(owner).setAllow(
                    EnumSet.of(Permission.VOICE_CONNECT, Permission.VOICE_DEAF_OTHERS)
            ).queue();
            update();
        });
    }

    public void setPublicChannel(boolean isPublicChannel) {
        this.isPublicChannel = isPublicChannel;
        update();
    }

    public void addMembers(List<Member> members) {
        this.whitelist.addAll(members);
        update();
    }

    public void removeMembers(List<Member> members) {
        this.whitelist.removeAll(members);
        update();
    }

    public void addMember(Member member) {
        this.whitelist.add(member);
        update();
    }

    public void removeMember(Member member) {
        this.whitelist.remove(member);
        update();
    }

    public String getChannelName() {
        return channelName;
    }

    public Member getOwner() {
        return owner;
    }

    public List<Member> getWhitelist() {
        return whitelist;
    }

    public boolean isPublicChannel() {
        return isPublicChannel;
    }

    public VoiceChannel getVoiceChannel() {
        return voiceChannel;
    }

    public void update() {
        // set public channel stats
        PermissionOverrideAction publicPerms = this.voiceChannel.upsertPermissionOverride(guild.getPublicRole());
        if (this.isPublicChannel)
            publicPerms = publicPerms.setAllow(Permission.VOICE_CONNECT);
        else publicPerms = publicPerms.setDeny(Permission.VOICE_CONNECT);
        publicPerms.queue();

        // update add/remove members
        this.whitelist.forEach( member -> {
            this.voiceChannel.upsertPermissionOverride(member).setAllow(Permission.VOICE_CONNECT).queue();
        });
        this.voiceChannel.getMemberPermissionOverrides().forEach( po -> {
            if (po.getMember() != null && !this.whitelist.contains(po.getMember()))
                this.voiceChannel.upsertPermissionOverride(po.getMember()).reset().queue();
        });

        // rename channel
        if (!this.voiceChannel.getName().equalsIgnoreCase(this.channelName))
            this.voiceChannel.getManager().setName(this.channelName).queue();
    }

    public void remove() {
        this.channelMap.remove(this.owner);
        this.voiceChannel.delete().queue();
    }



}
