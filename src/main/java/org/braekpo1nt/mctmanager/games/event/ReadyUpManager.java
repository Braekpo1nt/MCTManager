package org.braekpo1nt.mctmanager.games.event;

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
    
    public ReadyUpManager(GameManager gameManager) {
        this.gameManager = gameManager;
    }
    
    public void start(List<UUID> participantUUIDs) {
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
    
    /**
     * @param participantUUID the UUID of a valid participant to check the status of.
     *                        Can be offline.
     * @return true if the participant is ready, false otherwise. 
     * Returns false for UUIDs which are not stored in this manager.
     */
    public boolean participantIsReady(UUID participantUUID) {
        String teamId = gameManager.getTeamName(participantUUID);
        TeamStatus teamStatus = teamStatuses.get(teamId);
        if (teamStatus == null) {
            return false;
        }
        return teamStatus.getStatuses().getOrDefault(participantUUID, false);
    }
    
    /**
     * @param teamId the teamId to get the ready count for
     * @return the number of participants on that team who are ready
     */
    public long readyCount(String teamId) {
        TeamStatus teamStatus = teamStatuses.get(teamId);
        if (teamStatus == null) {
            return 0;
        }
        return teamStatus.getStatuses().values().stream().filter(ready -> ready).count();
    }
    
    /**
     * @param teamId the teamId to get the unReady count for
     * @return the number of participants on this team who are NOT ready
     */
    public long unReadyCount(String teamId) {
        TeamStatus teamStatus = teamStatuses.get(teamId);
        if (teamStatus == null) {
            return 0;
        }
        return teamStatus.getStatuses().values().stream().filter(ready -> !ready).count();
    }
    
    /**
     * @param teamId the teamId to get the readiness of
     * @return true if the team is ready (all members of the team are ready), false otherwise.
     * False if the team is not tracked in this manager.
     */
    public boolean teamIsReady(String teamId) {
        TeamStatus teamStatus = teamStatuses.get(teamId);
        if (teamStatus == null) {
            return false;
        }
        Map<UUID, @NotNull Boolean> statuses = teamStatus.getStatuses();
        return statuses.values().stream().filter(ready -> ready).count() == statuses.size();
    }
    
    /**
     * Marks the participant with the given UUID as ready
     * @param participantUUID a valid participant UUID
     */
    public void readyUpParticipant(UUID participantUUID) {
        readyParticipant(participantUUID, true);
    }
    
    /**
     * Marks the participant with the given UUID as not ready
     * @param participantUUID a valid participant UUID
     */
    public void unReadyParticipant(UUID participantUUID) {
        readyParticipant(participantUUID, false);
    }
    
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
