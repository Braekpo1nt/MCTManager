package org.braekpo1nt.mctmanager.games.event;

import com.google.common.base.Preconditions;
import lombok.Data;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ReadyUpManager {
    
    private final Map<String, @NotNull TeamStatus> teamStatuses = new HashMap<>();
    
    @Data
    public static class TeamStatus {
    
        private final Map<UUID, @NotNull Boolean> statuses = new HashMap<>();
        /**
         * @return true if every participant is ready in this team
         */
        public boolean isReady() {
            return statuses.values().stream().allMatch(ready -> ready);
        }
    
        /**
         * @return the number of ready statuses
         */
        public long readyCount() {
            return statuses.values().stream().filter(ready -> ready).count();
        }
    
    }
    private @NotNull TeamStatus getTeamStatus(@NotNull String teamId) {
        TeamStatus teamStatus = teamStatuses.get(teamId);
        Preconditions.checkArgument(teamStatus != null, "teamId \"%s\" is not contained in this ReadyUpManager", teamId);
        return teamStatus;
    }
    
    private boolean getParticipantStatus(@NotNull UUID participantUUID, @NotNull String teamId) {
        TeamStatus teamStatus = getTeamStatus(teamId);
        Boolean status = teamStatus.getStatuses().get(participantUUID);
        Preconditions.checkArgument(status != null, "participant with UUID \"%s\" & teamId \"%s\" is not contained in this ReadyUpManager", participantUUID, teamId);
        return status;
    }
    
    public void clear() {
        teamStatuses.clear();
    }
    
    /**
     * @param participantUUID the UUID of a valid participant to check the status of.
     * @param teamId must be a teamId in this manager
     * @return true if the participant is ready, false otherwise. 
     * Returns false for UUIDs which are not stored in this manager.
     */
    public boolean participantIsReady(@NotNull UUID participantUUID, @NotNull String teamId) {
        return getParticipantStatus(participantUUID, teamId);
    }
    
    /**
     * Gets the number of ready players on this team.
     * @param teamId the teamId to get the ready count for. Must be in this manager
     * @return the number of participants on that team who are ready
     */
    public long readyCount(@NotNull String teamId) {
        TeamStatus teamStatus = getTeamStatus(teamId);
        return teamStatus.readyCount();
    }
    
    /**
     * Add the given teamId to the manager. Defaults to 0 readyCount.
     * @param teamId the teamId to add. Must not already be tracked in this manager.
     */
    public void addTeam(@NotNull String teamId) {
        Preconditions.checkArgument(!teamStatuses.containsKey(teamId), "teamId \"%s\" already exists in this ReadyUpManager");
        teamStatuses.put(teamId, new TeamStatus());
    }
    
    /**
     * @param teamId the teamId to check
     * @return true if the given teamId is contained in this manager, false otherwise
     */
    public boolean containsTeam(@NotNull String teamId) {
        return teamStatuses.containsKey(teamId);
    }
    
    /**
     * @param teamId the teamId to get the readiness of. Must be a valid teamId stored in this manager.
     * @return true if the team is ready (all members of the team are ready), false otherwise.
     */
    public boolean teamIsReady(@NotNull String teamId) {
        TeamStatus teamStatus = getTeamStatus(teamId);
        return teamStatus.isReady();
    }
    
    /**
     * @return how many teams are ready
     */
    public long readyTeamCount() {
        return teamStatuses.values().stream().filter(TeamStatus::isReady).count();
    }
    
    /**
     * @return true if all teams in this manager are ready, false otherwise
     */
    public boolean allTeamsAreReady() {
        return teamStatuses.values().stream().allMatch(TeamStatus::isReady);
    }
    
    /**
     * Marks the participant with the given UUID as ready. Adds the given participant to this
     * manager if they didn't already exist, and adds their team if it didn't already exist.
     * @param participantUUID a valid participant UUID. Can be offline
     * @param teamId a teamId in this manager
     * @return the ready status of the given participantUUID before this ready assignment
     */
    public boolean readyUpParticipant(@NotNull UUID participantUUID, @NotNull String teamId) {
        return setReadyStatus(participantUUID, teamId, true);
    }
    
    /**
     * Marks the participant with the given UUID as not ready. Add them if
     * they weren't already being tracked. 
     * @param participantUUID a valid participant UUID. Can be offline
     * @param teamId a teamId in this manager
     * @return the ready status of the given participantUUID before this unReady assignment
     */
    public boolean unReadyParticipant(@NotNull UUID participantUUID, @NotNull String teamId) {
        return setReadyStatus(participantUUID, teamId, false);
    }
    
    /**
     * assign the given status to the given participant's UUID. Add them if
     * they weren't already being tracked. 
     * @param participantUUID a valid participant UUID. Can be offline
     * @param teamId a teamId in this manager
     * @param ready the ready status
     */
    private boolean setReadyStatus(@NotNull UUID participantUUID, @NotNull String teamId, boolean ready) {
        TeamStatus teamStatus = getTeamStatus(teamId);
        Boolean previous = teamStatus.getStatuses().put(participantUUID, ready);
        return previous != null && previous;
    }
}
