package org.braekpo1nt.mctmanager.games.event;

import com.google.common.base.Preconditions;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ReadyUpManager {
    
    private final Map<String, @NotNull TeamStatus> teamStatuses = new HashMap<>();
    
    @Data
    public static class TeamStatus {
        private final Map<UUID, @NotNull Boolean> statuses = new HashMap<>();
    }
    
    public @NotNull TeamStatus getTeamStatus(@NotNull String teamId) {
        TeamStatus teamStatus = teamStatuses.get(teamId);
        Preconditions.checkArgument(teamStatus != null, "teamId \"%s\" is not contained in this ReadyUpManager", teamId);
        return teamStatus;
    }
    
    public boolean getParticipantStatus(@NotNull UUID participantUUID, @NotNull String teamId) {
        TeamStatus teamStatus = getTeamStatus(teamId);
        Boolean status = teamStatus.getStatuses().get(participantUUID);
        Preconditions.checkArgument(status != null, "\"%s\" participant with UUID %s is not contained in this ReadyUpManager", participantUUID);
        return status;
    }
    
    /**
     * @param participantUUID the UUID of a valid participant to check the status of.
     * @return true if the participant is ready, false otherwise. 
     * Returns false for UUIDs which are not stored in this manager.
     */
    public boolean participantIsReady(@NotNull UUID participantUUID, @NotNull String teamId) {
        return getParticipantStatus(participantUUID, teamId);
    }
    
    /**
     * Gets the number of ready players on this team.
     * @param teamId the teamId to get the ready count for
     * @return the number of participants on that team who are ready
     */
    public long readyCount(@NotNull String teamId) {
        TeamStatus teamStatus = getTeamStatus(teamId);
        return teamStatus.getStatuses().values().stream().filter(ready -> ready).count();
    }
    
    /**
     * Gets the number of unReady players on this team.
     * @param teamId the teamId to get the unReady count for
     * @return the number of participants on this team who are NOT ready
     */
    public long unReadyCount(@NotNull String teamId) {
        TeamStatus teamStatus = getTeamStatus(teamId);
        return teamStatus.getStatuses().values().stream().filter(ready -> !ready).count();
    }
    
    /**
     * @param teamId the teamId to get the readiness of. Must be a valid teamId stored in this manager.
     * @return true if the team is ready (all members of the team are ready), false otherwise.
     */
    public boolean teamIsReady(@NotNull String teamId) {
        TeamStatus teamStatus = getTeamStatus(teamId);
        Map<UUID, @NotNull Boolean> statuses = teamStatus.getStatuses();
        return statuses.values().stream().filter(ready -> ready).count() == statuses.size();
    }
    
    /**
     * Marks the participant with the given UUID as ready. Adds the given participant to this
     * manager if they didn't already exist, and adds their team if it didn't already exist.
     * @param participantUUID a valid participant UUID. Can be offline
     */
    public void readyUpParticipant(@NotNull UUID participantUUID, @NotNull String teamId) {
        readyParticipant(participantUUID, teamId, true);
    }
    
    /**
     * Marks the participant with the given UUID as not ready. Adds the given participant to this
     * manager if they didn't already exist, and adds their team if it didn't already exist.
     * @param participantUUID a valid participant UUID. Can be offline
     */
    public void unReadyParticipant(@NotNull UUID participantUUID, @NotNull String teamId) {
        readyParticipant(participantUUID, teamId, false);
    }
    
    /**
     * assign the given status to the given participant's UUID. Add them and/or their team if
     * they weren't already being tracked. 
     * @param participantUUID a valid participant UUID. Can be offline
     * @param ready the ready status
     */
    private void readyParticipant(@NotNull UUID participantUUID, @NotNull String teamId, boolean ready) {
        TeamStatus teamStatus = teamStatuses.get(teamId);
        if (teamStatus == null) {
            teamStatus = new TeamStatus();
            teamStatuses.put(teamId, teamStatus);
        }
        teamStatus.getStatuses().put(participantUUID, ready);
    }
}
