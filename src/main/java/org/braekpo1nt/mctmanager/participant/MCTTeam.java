package org.braekpo1nt.mctmanager.participant;

import lombok.ToString;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.utils.GameManagerUtils;
import org.braekpo1nt.mctmanager.utils.AudienceDelegate;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * The GameManager's implementation of {@link Team}. Handles online membership
 * and stores online members.
 */
@ToString(callSuper = true)
public class MCTTeam extends TeamInfo implements AudienceDelegate {
    
    /**
     * The UUIDs of the {@link Participant}s on this team, both on and offline
     */
    private final @NotNull Set<UUID> members;
    /**
     * The Participants who are members of this team and are online
     */
    private final @NotNull Map<UUID, Participant> onlineMembers;
    private @NotNull Audience audience;
    
    /**
     * Create a new Team
     * @param teamId the unique id of the team
     * @param displayName the pretty display name of the team in text form
     * @param color the team's assigned color
     * @param members a set of the UUIDs of the members of this team
     * @param score the team's score
     */
    public MCTTeam(@NotNull String teamId, @NotNull String displayName, @NotNull TextColor color, @NotNull Set<UUID> members, int score) {
        super(teamId, displayName, color, score);
        this.members = members;
        this.onlineMembers = new HashMap<>();
        this.audience = Audience.empty();
    }
    
    /**
     * Create a new Team with the given initial members
     * @param teamId the unique id of the team
     * @param displayName the pretty display name of the team in text form
     * @param color the team's assigned color
     * @param members a collection of the UUIDs of the members of this team
     */
    public MCTTeam(@NotNull String teamId, @NotNull String displayName, @NotNull TextColor color, @NotNull Collection<UUID> members, int score) {
        this(teamId, displayName, color, new HashSet<>(members), score);
    }
    
    /**
     * Create a new Team with no initial members
     * @param teamId the unique id of the team
     * @param displayName the pretty display name of the team in text form
     * @param color the team's assigned color
     */
    public MCTTeam(@NotNull String teamId, @NotNull String displayName, @NotNull TextColor color, int score) {
        this(teamId, displayName, color, new HashSet<>(), score);
    }
    
    /**
     * Copy everything from the team, but change the score
     * @param team the team to copy
     * @param newScore the new score
     */
    public MCTTeam(MCTTeam team, int newScore) {
        super(team.getTeamId(), team.getDisplayName(), team.getColor(), team.getBukkitColor(), team.getFormattedDisplayName(), newScore);
        this.members = new HashSet<>(team.members);
        this.onlineMembers = new HashMap<>(team.onlineMembers);
        this.audience = Audience.audience(this.onlineMembers.values());
    }
    
    /**
     * @param teams the teams to filter for online
     * @return the set of teams from the given collection who has at least one online member
     */
    public static Set<MCTTeam> getOnlineTeams(Map<String, MCTTeam> teams) {
        return getOnlineTeams(teams.values());
    }
    
    /**
     * @param teams the teams to filter for online
     * @return the set of teams from the given collection who has at least one online member
     */
    public static Set<MCTTeam> getOnlineTeams(Collection<MCTTeam> teams) {
        return teams.stream().filter(MCTTeam::isOnline).collect(Collectors.toSet());
    }
    
    /**
     * @return the audience including all online members
     * @see #onlineMembers
     */
    @Override
    public @NotNull Audience getAudience() {
        return audience;
    }
    
    public @NotNull Component createDisplayName(String name) {
        return GameManagerUtils.createDisplayName(name, getColor());
    }
    
    /**
     * Add a new member of this team
     * @param uuid the UUID of the new member
     */
    public void joinMember(@NotNull UUID uuid) {
        members.add(uuid);
    }
    
    /**
     * Add multiple new members of this team
     * @param uuids the UUIDs of the new members
     */
    public void joinMembers(@NotNull Collection<UUID> uuids) {
        members.addAll(uuids);
    }
    
    /**
     * Remove an old member of this team. If the member was also an online member, then it quits them
     * using {@link #quitOnlineMember(UUID)}.
     * <br>
     * Warning, this should not be used inside of games, only the GameManager.
     * TODO: prevent users from accidentally leaving players from teams outside the GameManager
     * @param uuid the UUID of the old member
     * @return true if the given UUID was previously a member of this team 
     * (see {@link #isMember(UUID)}), false otherwise
     */
    @SuppressWarnings("UnusedReturnValue")
    public boolean leaveMember(@NotNull UUID uuid) {
        boolean removed = members.remove(uuid);
        if (removed) {
            quitOnlineMember(uuid);
        }
        return removed;
    }
    
    /**
     * Remove multiple old members of this team
     * <br>
     * Warning, this should not be used inside of games, only the GameManager.
     * TODO: prevent users from accidentally leaving players from teams outside the GameManager
     * @param uuids the UUIDs of the members to remove from this team
     * @return true if any members were removed, false otherwise
     */
    public boolean leaveMembers(@NotNull Collection<UUID> uuids) {
        boolean changed = members.removeAll(uuids);
        if (changed) {
            quitOnlineMembers(uuids);
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
     *                    Their {@link Participant#getTeamId()} 
     *                    must match this team's {@link #getTeamId()}.
     * @throws IllegalStateException if the given participant is not a member of this team 
     * (i.e. their teamId matches this team's)
     */
    public void joinOnlineMember(@NotNull Participant participant) {
        if (!members.contains(participant.getUniqueId())) {
            throw new IllegalArgumentException(String.format("Can't join participant \"%s\" with UUID \"%s\" and teamId \"%s\" to team \"%s\" because they are not in the members set",
                    participant.getName(), participant.getUniqueId(), this.getTeamId(), participant.getTeamId()));
        }
        if (!getTeamId().equals(participant.getTeamId())) {
            throw new IllegalArgumentException(String.format("Can't join participant \"%s\" with UUID \"%s\" and teamId \"%s\" to team \"%s\" because their teamId doesn't match",
                    participant.getName(), participant.getUniqueId(), this.getTeamId(), participant.getTeamId()));
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
            audience = onlineMembers.isEmpty() ? Audience.empty() : Audience.audience(onlineMembers.values());
        }
        return removed;
    }
    
    /**
     * For when multiple Participants log off/quit/are removed who were previously online.
     * 
     * @param uuids the UUIDs of the previously online members
     * @return true if any of the given UUIDs were those of previously online members of this team, false otherwise
     */
    @SuppressWarnings("UnusedReturnValue")
    public boolean quitOnlineMembers(@NotNull Collection<UUID> uuids) {
        boolean removed = false;
        for (UUID uuid : uuids) {
            removed = removed || (onlineMembers.remove(uuid) != null);
        }
        if (removed) {
            audience = onlineMembers.isEmpty() ? Audience.empty() : Audience.audience(onlineMembers.values());
        }
        return removed;
    }
    
    /**
     * @return a collection of the online members of this team
     */
    public Collection<Participant> getOnlineMembers() {
        return onlineMembers.values();
    }
    
    /**
     * @return true if there are any online members in this team, false otherwise
     */
    public boolean isOnline() {
        return !onlineMembers.isEmpty();
    }
    
    /**
     * Sends the given message to every online member of the team except for the given participant
     * @param sender the participant who sent the message, and therefore should not receive the message
     *                    (doesn't have to be a member of the team)
     * @param message the message to send
     */
    public void sendMessageFrom(@NotNull Participant sender, @NotNull Component message) {
        if (onlineMembers.containsKey(sender.getUniqueId())) {
            Audience.audience(onlineMembers.values().stream()
                            .filter(m -> !m.getUniqueId().equals(sender.getUniqueId())).toList())
                    .sendMessage(message);
        } else {
            audience.sendMessage(message);
        }
    }
}
