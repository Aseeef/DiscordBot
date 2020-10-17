package utils.channels;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Invite;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.restaction.PermissionOverrideAction;
import org.jetbrains.annotations.NotNull;
import utils.MembersCache;
import utils.confighelpers.Config;
import utils.selfdata.ChannelData;
import utils.selfdata.ChannelIdData;
import utils.tools.GTools;
import utils.users.Rank;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static utils.tools.GTools.guild;
import static utils.tools.GTools.jda;

public class CustomChannel extends ListenerAdapter {

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

    @JsonIgnore
    private ScheduledFuture deleteTimer;

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
            GTools.runDelayedTask( () -> {
                ChannelData.get().getChannelMap().remove(this.ownerId);
                ChannelData.get().save();
            }, 10);
            return;
        }

        this.voiceChannel.retrieveInvites().queue( (invites -> this.invite = invites.get(0)));

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
            GTools.runDelayedTask( () -> {
                ChannelData.get().getChannelMap().remove(this.ownerId);
                ChannelData.get().save();
            }, 10);
            return;
        }

        // if no one is in this channel, start delete task
        if (this.voiceChannel.getMembers().size() == 0)
            this.deleteTimer = startDeleteTimer();

        jda.addEventListener(this);

    }

    @JsonIgnore
    public CustomChannel(String channelName, Member owner, boolean isPublicChannel) {
        this.channelName = channelName;
        this.ownerId = owner.getIdLong();
        this.owner = owner;
        this.isPublicChannel = isPublicChannel;

        // start delete timer in case no one ends up joining
        this.deleteTimer = startDeleteTimer();

        jda.addEventListener(this);
    }


    /**
     * This event listener checks for updates to voice channel in order to determine whether a channel needs to be deleted.
     */
    public void onGuildVoiceUpdate (@NotNull GuildVoiceUpdateEvent event) {
        if (event.getChannelLeft() != voiceChannel && event.getChannelJoined() != voiceChannel) return;

        if (event.getChannelJoined() != null && event.getChannelJoined() == voiceChannel) {
            if (this.deleteTimer != null && this.voiceChannel.getMembers().size() == 0)
                this.deleteTimer.cancel(false);
        }

        else if (event.getChannelLeft() != null && event.getChannelLeft() == voiceChannel && event.getChannelLeft().getMembers().size() == 0) {
            // cancel any previous timers if any
            if (this.deleteTimer != null)
                this.deleteTimer.cancel(false);
            // start timer to delete the channel
            this.deleteTimer = startDeleteTimer();
        }
    }

    /**
     * This method starts a timer to delete this channel after a time as configured in config if no players are in the channel
     */
    private ScheduledFuture startDeleteTimer() {
        return GTools.runDelayedTask( () -> {

            if (this.voiceChannel.getMembers().size() == 0) {
                this.remove();
                this.owner.getUser().openPrivateChannel().queue(privateChannel ->
                        privateChannel.sendMessage("**Your custom channel `" + this.channelName + "` was deleted due to inactivity. Just do `/channel create (name)` to create a new one!**")
                                .queue());
            }

        }, 1000 * 60 * Config.get().getCustomChannelDeleteTime());
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
    public void setChannelName(String channelName) {
        this.channelName = channelName;
        update();
    }

    @JsonIgnore
    /**
     * @return - Return an optional custom channel based on the owner's member id.
     */
    public static Optional<CustomChannel> get(Member owner) {
        return Optional.ofNullable(ChannelData.get().getChannelMap().getOrDefault(owner.getIdLong(), null));
    }

    @JsonIgnore
    /**
     * Create the custom channel with the given parameters (if it doesn't exist already) as well as set initial channel settings.
     * @return - Returns a Future voice channel.
     */
    public CompletableFuture<VoiceChannel> create() {
        CompletableFuture<VoiceChannel> futureChannel = new CompletableFuture<>();

        if (this.voiceChannel != null) futureChannel.complete(this.voiceChannel);
        else
            ChannelIdData.get().getPrivateChannelsCategory().createVoiceChannel(this.channelName)
                    .queue(vc -> {
                        this.voiceChannel = vc;
                        this.voiceChannelId = vc.getIdLong();
                        vc.upsertPermissionOverride(owner).setAllow(
                                EnumSet.of(Permission.VOICE_CONNECT, Permission.VOICE_MOVE_OTHERS)
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

    @JsonIgnore
    /**
     * Set the channel to public so everyone can join
     */
    public void setPublicChannel(boolean isPublicChannel) {
        this.isPublicChannel = isPublicChannel;
        update();
    }

    @JsonIgnore
    /**
     * Set the maximum channel size
     */
    public void setChannelMax(int i) {
        this.voiceChannel.getManager().setUserLimit(i).queue();
    }

    @JsonIgnore
    /**
     * Add a new member to this custom channel msging the member they were added to the channel
     */
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
    /**
     * Remove the given member from this custom channel
     */
    public void removeMember(Member member) {
        this.blacklist.add(member);
        this.whitelist.remove(member);
        update();
    }

    @JsonIgnore
    /**
     * Reset this custom channel to its default conditions as left by the create() method initially.
     */
    public void reset() {
        this.blacklist.clear();
        this.whitelist.clear();
        this.isPublicChannel = false;
        this.voiceChannel.getManager().setUserLimit(0).queue();
        update();
    }

    @JsonIgnore
    /**
     * @return The owner and creator of the custom channel (as a discord Member)
     */
    public Member getOwner() {
        return owner;
    }

    @JsonIgnore
    /**
     * @return the voice channel of this custom channel
     */
    public VoiceChannel getVoiceChannel() {
        return voiceChannel;
    }

    @JsonIgnore
    /**
     * @return the invite associated with this custom channel
     */
    public Invite getInvite() {
        return invite;
    }

    @JsonIgnore
    /**
     * Update this custom channel from its list of whitelist and blacklist
     * This method should be called to update most changes this this object.
     */
    public void update() {

        // transfer the whitelist/blacklist member list to the member id list
        this.whitelist.forEach( (w) -> this.whitelistIds.add(w.getIdLong()));
        this.blacklist.forEach( (w) -> this.blacklistIds.add(w.getIdLong()));

        // If neither in the blacklist or whitelist, delete permission override for member
        this.voiceChannel.getMemberPermissionOverrides().forEach( (po) -> {
            if (po.getMember() == owner) return;
            if (!this.blacklist.contains(po.getMember()) && !this.whitelist.contains(po.getMember()))
                po.delete().queue();
        });

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
        ChannelData.get().getChannelMap().put(owner.getIdLong(), this);
        ChannelData.get().save();
    }

    @JsonIgnore
    /**
     * Remove or delete this custom channel
     */
    public void remove() {
        this.voiceChannel.delete().queue();
        ChannelData.get().getChannelMap().remove(this.owner.getIdLong());
        ChannelData.get().save();

        System.out.println("Removed " + this.owner.getUser().getAsTag() + "'s custom channel " + this.channelName);
    }

    @JsonIgnore
    /**
     * @return - Returns whether a custom channel can be created based on whether a valid channel category is available
     */
    public static boolean canCreateChannels() {
        return ChannelIdData.get().getPrivateChannelsCategory() != null;
    }

}
