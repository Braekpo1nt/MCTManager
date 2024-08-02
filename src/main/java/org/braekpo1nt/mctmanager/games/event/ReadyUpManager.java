package org.braekpo1nt.mctmanager.games.event;

import com.google.common.base.Preconditions;
import lombok.Data;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ReadyUpManager {
    
    private final GameManager gameManager;
    
    private Map<String, @NotNull TeamStatus> teamStatuses = new HashMap<>();
    
    @Data
    public static class TeamStatus {
        private final Map<UUID, @NotNull Boolean> statuses = new HashMap<>();
    }
    
    public ReadyUpManager(GameManager gameManager, List<UUID> participantUUIDs) {
        this.gameManager = gameManager;
        List<String> teamIds = gameManager.getTeamIdsByUUID(participantUUIDs);
        teamStatuses = new HashMap<>(teamIds.size());
        for (String teamId : teamIds) {
            teamStatuses.put(teamId, new TeamStatus());
        }
        for (UUID participantUUID : participantUUIDs) {
            String teamId = gameManager.getTeamName(participantUUID);
            teamStatuses.get(teamId).getStatuses().put(participantUUID, false);
        }
    }
    
    public @NotNull TeamStatus getTeamStatus(@NotNull String teamId) {
        TeamStatus teamStatus = teamStatuses.get(teamId);
        Preconditions.checkArgument(teamStatus != null, "teamId \"%s\" is not contained in this ReadyUpManager", teamId);
        return teamStatus;
    }
    
    public boolean getParticipantStatus(@NotNull UUID participantUUID) {
        String teamId = gameManager.getTeamName(participantUUID);
        TeamStatus teamStatus = getTeamStatus(teamId);
        Boolean status = teamStatus.getStatuses().get(participantUUID);
        Preconditions.checkArgument(status != null, "\"%s\" participant with UUID %s is not contained in this ReadyUpManager", participantUUID);
        return status;
    }
    
    /**
     * @param participantUUID the UUID of a valid participant to check the status of.
     *                        Can be offline.
     * @return true if the participant is ready, false otherwise. 
     * Returns false for UUIDs which are not stored in this manager.
     */
    public boolean participantIsReady(UUID participantUUID) {
        return getParticipantStatus(participantUUID);
    }
    
    /**
     * Gets the number of ready players on this team. Adds the teamId to this manager
     * if it's not already present. 
     * @param teamId the teamId to get the ready count for
     * @return the number of participants on that team who are ready
     */
    public long readyCount(String teamId) {
        TeamStatus teamStatus = getTeamStatus(teamId);
        return teamStatus.getStatuses().values().stream().filter(ready -> ready).count();
    }
    
    /**
     * Gets the number of unReady players on this team. Adds the teamId to this manager
     * if it's not already present. 
     * @param teamId the teamId to get the unReady count for
     * @return the number of participants on this team who are NOT ready
     */
    public long unReadyCount(String teamId) {
        TeamStatus teamStatus = getTeamStatus(teamId);
        return teamStatus.getStatuses().values().stream().filter(ready -> !ready).count();
    }
    
    /**
     * @param teamId the teamId to get the readiness of. Must be a valid teamId stored in this manager.
     * @return true if the team is ready (all members of the team are ready), false otherwise.
     */
    public boolean teamIsReady(String teamId) {
        TeamStatus teamStatus = getTeamStatus(teamId);
        Map<UUID, @NotNull Boolean> statuses = teamStatus.getStatuses();
        return statuses.values().stream().filter(ready -> ready).count() == statuses.size();
    }
    
    /**
     * Marks the participant with the given UUID as ready. Adds the given participant to this
     * manager if they didn't already exist, and adds their team if it didn't already exist.
     * @param participantUUID a valid participant UUID
     */
    public void readyUpParticipant(UUID participantUUID) {
        readyParticipant(participantUUID, true);
    }
    
    /**
     * Marks the participant with the given UUID as not ready. Adds the given participant to this
     * manager if they didn't already exist, and adds their team if it didn't already exist.
     * @param participantUUID a valid participant UUID
     */
    public void unReadyParticipant(UUID participantUUID) {
        readyParticipant(participantUUID, false);
    }
    
    /**
     * assign the given status to the given participant's UUID. Add them and/or their team if
     * they weren't already being tracked. 
     * @param participantUUID a valid participant UUID
     * @param ready the ready status
     */
    private void readyParticipant(UUID participantUUID, boolean ready) {
        String teamId = gameManager.getTeamName(participantUUID);
        TeamStatus teamStatus = teamStatuses.get(teamId);
        if (teamStatus == null) {
            teamStatus = new TeamStatus();
            teamStatuses.put(teamId, teamStatus);
        }
        teamStatus.getStatuses().put(participantUUID, ready);
    }
}
