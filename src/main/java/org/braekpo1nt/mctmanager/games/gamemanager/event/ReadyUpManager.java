package org.braekpo1nt.mctmanager.games.gamemanager.event;

import lombok.Data;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.ui.UIException;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

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
    
    /**
     * @param participantUUID the participants UUID
     * @param teamId the participant's team
     * @return the participant's ready status, or false if the given team is not in this manager
     * or false if the given participant is not in this manager under that teamId
     */
    private boolean getParticipantStatus(@NotNull UUID participantUUID, @NotNull String teamId) {
        TeamStatus teamStatus = teamStatuses.get(teamId);
        if (teamStatus == null) {
            return false;
        }
        Boolean status = teamStatus.getStatuses().get(participantUUID);
        if (status == null) {
            return false;
        }
        return status;
    }
    
    public void cleanup() {
        teamStatuses.clear();
    }
    
    /**
     * @param participantUUID the participants UUID
     * @param teamId the participant's team
     * @return the participant's ready status, or false if the given team is not in this manager
     * or false if the given participant is not in this manager under that teamId
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
        TeamStatus teamStatus = teamStatuses.get(teamId);
        if (teamStatus == null) {
            logUIError("teamId \"%s\" is not contained in this ReadyUpManager", teamId);
            return 0;
        }
        return teamStatus.readyCount();
    }
    
    /**
     * Add the given teamId to the manager. Defaults to 0 readyCount.
     * @param teamId the teamId to add. Must not already be tracked in this manager.
     */
    public void addTeam(@NotNull String teamId) {
        if (teamStatuses.containsKey(teamId)) {
            logUIError("teamId \"%s\" already exists in this ReadyUpManager");
            return;
        }
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
        TeamStatus teamStatus = teamStatuses.get(teamId);
        if (teamStatus == null) {
            logUIError("teamId \"%s\" is not contained in this ReadyUpManager", teamId);
            return false;
        }
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
        TeamStatus teamStatus = teamStatuses.get(teamId);
        if (teamStatus == null) {
            logUIError("teamId \"%s\" is not contained in this ReadyUpManager", teamId);
            return false;
        }
        Boolean previous = teamStatus.getStatuses().put(participantUUID, ready);
        return previous != null && previous;
    }
    
    /**
     * Log a UI error
     * @param reason the reason for the error (a {@link String#format(String, Object...)} template
     * @param args optional args for the reason format string
     */
    private void logUIError(@NotNull String reason, Object... args) {
        Main.logger().log(Level.WARNING,
                "An error occurred in the ReadyUpManager. Failing gracefully.",
                new UIException(String.format(reason, args)));
    }
}
