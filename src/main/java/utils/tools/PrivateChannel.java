package utils.tools;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.VoiceChannel;
import utils.SelfData;
import utils.users.GTMUser;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static utils.tools.GTools.jda;

public class PrivateChannel {

    private static Map<Member,PrivateChannel> channelMap = new HashMap<>();

    private GTMUser ownerUser;
    private Member ownerMember;

    private String name;
    private List<Member> members;
    private boolean publicChannel = false;
    private Guild guild = jda.getGuilds().get(0);
    private VoiceChannel voiceChannel;


    public PrivateChannel(Member ownerMember, String name) {
        this.ownerMember = ownerMember;
        this.ownerUser = GTMUser.getGTMUser(ownerMember.getIdLong()).orElse(null);
        this.name = name;
    }

    public void create() {
        jda.getGuilds().get(0)
                .createVoiceChannel(name)
                .setParent(SelfData.get().getPrivateChannelsCategory())
                .addPermissionOverride(ownerMember, EnumSet.of(Permission.VIEW_CHANNEL), null)
                .addPermissionOverride(guild.getPublicRole(), null, EnumSet.of(Permission.VOICE_CONNECT))
                .queue( (vc) -> this.voiceChannel = vc); // this actually sends the request to discord.
    }

    public Member getOwnerMember() {
        return ownerMember;
    }

    public void setOwnerMember(Member ownerMember) {
        this.ownerMember = ownerMember;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Member> getMembers() {
        return members;
    }

    public void setMembers(List<Member> members) {
        this.members = members;
    }

    public boolean isPublicChannel() {
        return publicChannel;
    }

    public void setPublicChannel(boolean publicChannel) {
        this.publicChannel = publicChannel;
    }

    public VoiceChannel getVoiceChannel() {
        return voiceChannel;
    }

    public void setVoiceChannel(VoiceChannel voiceChannel) {
        this.voiceChannel = voiceChannel;
    }

    public void delete() {
        this.voiceChannel.delete().queue();
    }

}
