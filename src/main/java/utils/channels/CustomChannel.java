package utils.channels;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Invite;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.requests.restaction.PermissionOverrideAction;
import utils.SelfData;
import utils.users.Rank;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static utils.tools.GTools.guild;

public class CustomChannel {

    private static Map<Member, CustomChannel> channelMap = new HashMap<>();

    private String channelName;
    private Member owner;
    private List<Member> whitelist;
    private List<Member> blacklist;
    private boolean isPublicChannel;
    private VoiceChannel voiceChannel;
    private Invite invite;

    public CustomChannel(String channelName, Member owner, boolean isPublicChannel) {
        this.channelName = channelName;
        this.owner = owner;
        this.isPublicChannel = isPublicChannel;

        channelMap.put(owner, this);
    }

    public static Optional<CustomChannel> get(Member owner) {
        return Optional.ofNullable(channelMap.getOrDefault(owner, null));
    }

    public CompletableFuture<VoiceChannel> create() {
        CompletableFuture<VoiceChannel> futureChannel = new CompletableFuture<>();
        SelfData.get().getPrivateChannelsCategory().createVoiceChannel(this.channelName)
                .queue(vc -> {
                    this.voiceChannel = vc;
                    update();
                    vc.upsertPermissionOverride(owner).setAllow(
                            EnumSet.of(Permission.VOICE_CONNECT, Permission.VOICE_DEAF_OTHERS, Permission.VOICE_MOVE_OTHERS)
                    ).queue(po -> vc.createInvite().setMaxAge(1L, TimeUnit.DAYS)
                            .queue((invite) -> {
                                this.invite = invite;
                                futureChannel.complete(vc);
                            }));
                });
        return futureChannel;
    }

    public void setPublicChannel(boolean isPublicChannel) {
        this.isPublicChannel = isPublicChannel;
        update();
    }

    public void addMember(Member member) {
        member.getUser().openPrivateChannel().queue( privateChannel -> {
            String msg = "**" + member.getUser().getAsTag() + " has invited you to join their Custom Channel on the GTM Discord!**\n";
            msg += this.getInvite().getUrl();
            privateChannel.sendMessage(msg).queue();
        });
        this.blacklist.remove(member);
        this.whitelist.add(member);
        update();
    }

    public void removeMember(Member member) {
        this.blacklist.add(member);
        this.whitelist.remove(member);
        update();
    }

    public void reset() {
        this.blacklist.clear();
        this.whitelist.clear();
        this.isPublicChannel = false;
        update();
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
        update();
    }

    public String getChannelName() {
        return channelName;
    }

    public Member getOwner() {
        return owner;
    }

    public boolean isPublicChannel() {
        return isPublicChannel;
    }

    public VoiceChannel getVoiceChannel() {
        return voiceChannel;
    }

    public Invite getInvite() {
        return invite;
    }

    public void update() {
        // set public channel stats
        PermissionOverrideAction publicPerms = this.voiceChannel.upsertPermissionOverride(guild.getPublicRole());
        if (this.isPublicChannel)
            publicPerms = publicPerms.setAllow(Permission.VOICE_CONNECT);
        else publicPerms = publicPerms.setDeny(Permission.VOICE_CONNECT);
        publicPerms.queue();

        // allow admins to join
        this.voiceChannel.upsertPermissionOverride(Rank.ADMIN.getRole()).setAllow(Permission.VOICE_CONNECT).queue();
        this.voiceChannel.upsertPermissionOverride(Rank.MANAGER.getRole()).setAllow(Permission.VOICE_CONNECT).queue();

        // update add/remove members
        this.whitelist.forEach( member -> {
            this.voiceChannel.upsertPermissionOverride(member).setAllow(Permission.VOICE_CONNECT).queue();
        });
        this.blacklist.forEach( member -> {
            this.voiceChannel.upsertPermissionOverride(member).setDeny(Permission.VOICE_CONNECT).queue();
        });

        // rename channel
        if (!this.voiceChannel.getName().equalsIgnoreCase(this.channelName))
            this.voiceChannel.getManager().setName(this.channelName).queue();
    }

    public void remove() {
        channelMap.remove(this.owner);
        this.invite.delete().queue(v ->
                this.voiceChannel.delete().queue()
        );
    }



}
