package utils.channels;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Invite;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.requests.restaction.PermissionOverrideAction;
import utils.Data;
import utils.MembersCache;
import utils.selfdata.ChannelData;
import utils.selfdata.SelfData;
import utils.users.Rank;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static utils.tools.GTools.guild;

public class CustomChannel {

    private static Map<Member, CustomChannel> channelMap = new HashMap<>();

    private String channelName;
    private long ownerId;
    private List<Long> whitelistIds = new ArrayList<>();
    private List<Long> blacklistIds = new ArrayList<>();
    private boolean isPublicChannel;
    private long voiceChannelId;
    @JsonIgnore
    private Member owner;
    @JsonIgnore
    private List<Member> whitelist = new ArrayList<>();
    @JsonIgnore
    private List<Member> blacklist = new ArrayList<>();
    @JsonIgnore
    private VoiceChannel voiceChannel;
    @JsonIgnore
    private Invite invite;

    @JsonCreator
    public CustomChannel(@JsonProperty("channelName") String channelName, @JsonProperty("voiceChannelId") long voiceChannelId, @JsonProperty("ownerId") long ownerId, @JsonProperty("whitelistIds") List<Long> whitelistIds, @JsonProperty("blacklistIds") List<Long> blacklistIds, @JsonProperty("isPublicChannel") boolean isPublicChannel) {
        this.channelName = channelName;
        this.voiceChannelId = voiceChannelId;
        this.ownerId = ownerId;
        this.whitelistIds = whitelistIds;
        this.blacklistIds = blacklistIds;
        this.isPublicChannel = isPublicChannel;

        this.voiceChannel = guild.getVoiceChannelById(this.voiceChannelId);

        if (this.voiceChannel == null) {
            SelfData.get().getUserChannelsMap().remove(this.ownerId);
            return;
        }

        this.whitelistIds.forEach( (id) -> {
            MembersCache.getMember(id).ifPresent( (member) -> {
                this.whitelist.add(member);
            });
        });

        this.blacklistIds.forEach( (id) -> {
            MembersCache.getMember(id).ifPresent( (member) -> {
                this.blacklist.add(member);
            });
        });

        // Get and set owner. If owner doesn't exist remove channel from data.
        Optional<Member> optionalOwner = MembersCache.getMember(this.ownerId);
        if (optionalOwner.isPresent()) {
            this.owner = optionalOwner.get();
        }
        else {
            SelfData.get().getUserChannelsMap().remove(this.ownerId);
        }

        channelMap.put(owner, this);
    }

    public CustomChannel(String channelName, Member owner, boolean isPublicChannel) {
        this.channelName = channelName;
        this.ownerId = owner.getIdLong();
        this.owner = owner;
        this.isPublicChannel = isPublicChannel;

        channelMap.put(owner, this);
    }

    @JsonGetter
    public String getChannelName() {
        return channelName;
    }

    @JsonGetter
    public long getOwnerId() {
        return ownerId;
    }

    @JsonGetter
    public List<Long> getWhitelistIds() {
        return whitelistIds;
    }

    @JsonGetter
    public List<Long> getBlacklistIds() {
        return blacklistIds;
    }

    @JsonGetter
    public long getVoiceChannelId() {
        return voiceChannelId;
    }

    @JsonGetter
    public boolean isPublicChannel() {
        return isPublicChannel;
    }

    @JsonIgnore
    public static Optional<CustomChannel> get(Member owner) {
        return Optional.ofNullable(channelMap.getOrDefault(owner, null));
    }

    @JsonIgnore
    public CompletableFuture<VoiceChannel> create() {
        CompletableFuture<VoiceChannel> futureChannel = new CompletableFuture<>();
        SelfData.get().getPrivateChannelsCategory().createVoiceChannel(this.channelName)
                .queue(vc -> {
                    this.voiceChannel = vc;
                    this.voiceChannelId = vc.getIdLong();
                    vc.upsertPermissionOverride(owner).setAllow(
                            EnumSet.of(Permission.VOICE_CONNECT, Permission.VOICE_DEAF_OTHERS, Permission.VOICE_MOVE_OTHERS)
                    ).queue(po -> vc.createInvite().setMaxAge(1L, TimeUnit.DAYS)
                            .queue((invite) -> {
                                this.invite = invite;
                                // update channel
                                update();
                                futureChannel.complete(vc);
                            }));
                });
        return futureChannel;
    }

    public void setPublicChannel(boolean isPublicChannel) {
        this.isPublicChannel = isPublicChannel;
        update();
    }

    @JsonIgnore
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

    @JsonIgnore
    public void removeMember(Member member) {
        this.blacklist.add(member);
        this.whitelist.remove(member);
        update();
    }

    @JsonIgnore
    public void reset() {
        this.blacklist.clear();
        this.whitelist.clear();
        this.isPublicChannel = false;
        update();
    }

    @JsonIgnore
    public void setChannelName(String channelName) {
        this.channelName = channelName;
        update();
    }

    public Member getOwner() {
        return owner;
    }

    public VoiceChannel getVoiceChannel() {
        return voiceChannel;
    }

    public Invite getInvite() {
        return invite;
    }

    @JsonIgnore
    public void update() {

        // transfer the whitelist/blacklist member list to the member id list
        this.whitelist.forEach( (w) -> this.whitelistIds.add(w.getIdLong()));
        this.blacklist.forEach( (w) -> this.blacklistIds.add(w.getIdLong()));

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

        // update map
        SelfData.get().getUserChannelsMap().put(ownerId, this);
    }

    @JsonIgnore
    public void remove() {
        channelMap.remove(this.owner);
        this.invite.delete().queue(v ->
                this.voiceChannel.delete().queue()
        );
    }

    @JsonIgnore
    public static boolean canCreateChannels() {
        return ChannelData.get().getPrivateChannelsCategory() != null;
    }



}
