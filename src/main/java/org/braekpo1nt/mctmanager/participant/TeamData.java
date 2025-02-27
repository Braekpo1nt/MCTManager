package org.braekpo1nt.mctmanager.participant;

import lombok.ToString;
import net.kyori.adventure.audience.Audience;
import org.braekpo1nt.mctmanager.utils.AudienceDelegate;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Used by implementations for {@link org.braekpo1nt.mctmanager.games.game.interfaces.MCTGame}
 * to handle team data, such as membership.
 */
@ToString(callSuper = true)
public class TeamData<T extends Participant> extends TeamInfo implements AudienceDelegate {
    
    /**
     * The Participants who are members of this team and are online
     */
    private final @NotNull Map<UUID, T> participants;
    private @NotNull Audience audience = Audience.empty();
    
    public TeamData(Team team) {
        super(team);
        participants = new HashMap<>();
    }
    
    @Override
    public @NotNull Audience getAudience() {
        return audience;
    }
    
    /**
     * Add the participant to this team. Their teamId must match {@link #getTeamId()}
     * @param participant the participant to add
     */
    public void addParticipant(T participant) {
        if (!participant.getTeamId().equals(getTeamId())) {
            throw new IllegalArgumentException(String.format("Participant \"%s\" can't be added to this TeamData \"%s\" because their team is \"%s\"", participant.getName(), participant.getTeamId(), getTeamId()));
        }
        participants.put(participant.getUniqueId(), participant);
        audience = Audience.audience(participants.values());
    }
    
    /**
     * @param uuid the UUID of the participant to remove
     * @return true if the given UUID was contained in this {@link TeamData}, false otherwise
     */
    public boolean removeParticipant(UUID uuid) {
        boolean changed = participants.remove(uuid) != null;
        if (changed) {
            audience = Audience.audience(participants.values());
        }
        return changed;
    }
    
    /**
     * @return the participants who are members of this {@link TeamData}
     */
    public Collection<T> getParticipants() {
        return participants.values();
    }
    
    public Collection<UUID> getMemberUUIDs() {
        return participants.keySet();
    }
    
    /**
     * @return the number of participants in the team
     */
    public int size() {
        return participants.size();
    }
    
}
