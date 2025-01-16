package org.braekpo1nt.mctmanager.participant;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.utils.AudienceDelegate;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Represents a team of {@link Participant}s
 */
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public class Team extends AudienceDelegate {
    /**
     * The unique ID of the team
     */
    @EqualsAndHashCode.Include
    @Getter
    protected final @NotNull String teamId;
    /**
     * The display name of the team
     */
    @Getter
    protected final @NotNull String displayName;
    /**
     * The color associated with the team
     */
    @Getter
    protected final @NotNull TextColor color;
    /**
     * The formatted display name of the team for use in chat messages.
     * The {@link #displayName} in {@link #color} and bold.
     */
    @Getter
    protected final @NotNull Component formattedDisplayName;
    
    /**
     * The UUIDs of the {@link Participant}s on this team, both on and offline
     */
    protected final @NotNull Set<UUID> members;
    /**
     * The Participants who are members of this team and are online
     */
    protected final @NotNull Map<UUID, Participant> onlineMembers = new HashMap<>();
    protected @NotNull Audience audience = Audience.empty();
    
    /**
     * Create a new Team
     * @param teamId the unique id of the team
     * @param displayName the pretty display name of the team in text form
     * @param color the team's assigned color
     * @param members a set of the UUIDs of the members of this team
     */
    public Team(@NotNull String teamId, @NotNull String displayName, @NotNull TextColor color, @NotNull Set<UUID> members) {
        this.teamId = teamId;
        this.displayName = displayName;
        this.color = color;
        this.formattedDisplayName = Component.text(displayName, color, TextDecoration.BOLD);
        this.members = members;
    }
    
    /**
     * Create a new Team with the given initial members
     * @param teamId the unique id of the team
     * @param displayName the pretty display name of the team in text form
     * @param color the team's assigned color
     * @param members a collection of the UUIDs of the members of this team
     */
    public Team(@NotNull String teamId, @NotNull String displayName, @NotNull TextColor color, @NotNull Collection<UUID> members) {
        this(teamId, displayName, color, new HashSet<>(members));
    }
    
    /**
     * Create a new Team with no initial members
     * @param teamId the unique id of the team
     * @param displayName the pretty display name of the team in text form
     * @param color the team's assigned color
     */
    public Team(@NotNull String teamId, @NotNull String displayName, @NotNull TextColor color) {
        this(teamId, displayName, color, new HashSet<>());
    }
    
    /**
     * @return the audience including all online members
     * @see #onlineMembers
     */
    @Override
    public @NotNull Audience getAudience() {
        return audience;
    }
    
    /**
     * Add a new member of this team
     * @param uuid the UUID of the new member
     */
    public void addMember(@NotNull UUID uuid) {
        members.add(uuid);
    }
    
    /**
     * Add multiple new members of this team
     * @param uuids the UUIDs of the new members
     */
    public void addMembers(@NotNull Collection<UUID> uuids) {
        members.addAll(uuids);
    }
    
    /**
     * Remove an old member of this team. If the member was also an online member, then it quits them
     * using {@link #quitOnlineMember(UUID)}.
     * @param uuid the UUID of the old member
     * @return true if the given UUID was previously a member of this team 
     * (see {@link #isMember(UUID)}), false otherwise
     */
    public boolean removeMember(@NotNull UUID uuid) {
        boolean removed = members.remove(uuid);
        if (removed) {
            quitOnlineMember(uuid);
        }
        return removed;
    }
    
    /**
     * Remove multiple old members of this team
     * @param uuids the UUIDs of the members to remove from this team
     * @return true if any members were removed, false otherwise
     */
    public boolean removeMembers(@NotNull Collection<UUID> uuids) {
        boolean changed = members.removeAll(uuids);
        if (changed) {
            for (UUID uuid : uuids) {
                quitOnlineMember(uuid);
            }
        }
        return changed;
    }
    
    /**
     * @param participant the participant who may or may not be a member of this team
     * @return true if the given participant is a member of this team, false otherwise
     */
    public boolean isMember(@NotNull Participant participant) {
        return isMember(participant.getUniqueId());
    }
    
    /**
     * @param uuid the uuid of one who may or may not be a member of this team
     * @return true if the given uuid is a member of this team, false otherwise
     */
    public boolean isMember(@NotNull UUID uuid) {
        return members.contains(uuid);
    }
    
    
    public Set<UUID> getMemberUUIDs() {
        return new HashSet<>(members);
    }
    
    /**
     * For when a Participant logs on/joins/is added who was previously offline
     * 
     * @param participant the participant who is an online member of this team. 
     *                    Their {@link Participant#getUniqueId()} must be in this team already 
     *                    (see {@link #addMember(UUID)}) and their {@link Participant#getTeamId()} 
     *                    must match this team's {@link #getTeamId()}.
     * @throws IllegalStateException if the given participant is not a member of this team 
     * (i.e. their UUID is already in this team and their teamId matches this team's)
     */
    public void joinOnlineMember(@NotNull Participant participant) {
        if (!members.contains(participant.getUniqueId()) 
                || !teamId.equals(participant.getTeamId())) {
            throw new IllegalStateException(String.format("Can't join participant \"%s\" with UUID \"%s\" and teamId \"%s\" because they are not a member of this team",
                    participant.getName(), participant.getUniqueId(), participant.getTeamId()));
        }
        onlineMembers.put(participant.getUniqueId(), participant);
        audience = Audience.audience(onlineMembers.values());
    }
    
    /**
     * For when a Participant logs off/quits/is removed who was previously online.
     * 
     * @param uuid the UUID of the previously online member
     * @return true if the given UUID was that of a previously online member of this team, false otherwise
     */
    public boolean quitOnlineMember(@NotNull UUID uuid) {
        boolean removed = onlineMembers.remove(uuid) != null;
        if (removed) {
            audience = Audience.audience(onlineMembers.values());
        }
        return removed;
    }
    
    /**
     * Sends the given message to every online member of the team except for the given participant
     * @param participant the participant who sent the message, and therefore should not receive the message
     *                    (doesn't have to be a member of the team)
     * @param message the message to send
     */
    public void sendMessageFrom(@NotNull Participant participant, @NotNull Component message) {
        Audience.audience(onlineMembers.values().stream()
                .filter(member -> !member.getUniqueId().equals(participant.getUniqueId())).toList())
                .sendMessage(message);
    }
    
}
