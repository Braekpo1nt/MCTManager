package org.braekpo1nt.mctmanager.games.event;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Responsible for keeping score of a particular game
 */
public class ScoreKeeper {
    protected final Map<String, Integer> teamScores;
    protected final Map<UUID, Integer> participantScores;
    
    public ScoreKeeper(@NotNull Map<String, Integer> teamScores, @NotNull Map<UUID, Integer> participantScores) {
        this.teamScores = teamScores;
        this.participantScores = participantScores;
    }
    
    /**
     * @deprecated use multi-arg constructor instead
     */
    @Deprecated
    public ScoreKeeper() {
        this(new HashMap<>(), new HashMap<>());
    }
    
    /**
     * Track the given scores for the teams and participants
     * @param teamScores the scores associated with each team
     * @param participantScores the scores associated with each participant
     * @deprecated use multi-arg constructor instead
     */
    @Deprecated
    public void trackScores(@NotNull Map<String, Integer> teamScores, @NotNull Map<UUID, Integer> participantScores) {
        this.teamScores.putAll(teamScores);
        this.participantScores.putAll(participantScores);
    }
    
    /**
     * Gets the score for a given participant. Defaults to 0 if no points have been added
     * for the participant. 
     * @param participantUUID the participant to get the score for
     * @return the participant's score
     */
    public int getScore(@NotNull UUID participantUUID) {
        if (!participantScores.containsKey(participantUUID)) {
            return 0;
        }
        return participantScores.get(participantUUID);
    }
    
    /**
     * Gets the score for a given team. Defaults to 0 if the no points have been added
     * for the team.
     * @param teamId The teamId to get the score of
     * @return the team's score
     */
    public int getScore(@NotNull String teamId) {
        if (!teamScores.containsKey(teamId)) {
            return 0;
        }
        return teamScores.get(teamId);
    }
    
    public Map<UUID, Integer> getParticipantScores() {
        return Collections.unmodifiableMap(participantScores);
    }
    
    public Map<String, Integer> getTeamScores() {
        return Collections.unmodifiableMap(teamScores);
    }
}
