package org.braekpo1nt.mctmanager.games.event;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Responsible for keeping score of a particular game
 */
public class ScoreKeeper {
    protected final Map<UUID, Integer> participantScores = new HashMap<>();
    protected final Map<String, Integer> teamScores = new HashMap<>();
    
    /**
     * Adds the given points to the given participant's kept score.
     * If this is the first points added for the participant, the score is set to the points.
     * @param participantUUID The participant to add the points to
     * @param points the points to add to the score
     */
    public void addPoints(UUID participantUUID, int points) {
        if (!participantScores.containsKey(participantUUID)) {
            participantScores.put(participantUUID, points);
            return;
        }
        int oldScore = participantScores.get(participantUUID);
        participantScores.put(participantUUID, oldScore + points);
    }
    
    /**
     * Adds the given points to the teams kept score.
     * If this is the first points added for the team, the score is set to the points
     * @param team The team to add the points to
     * @param points The points to add to the score
     */
    public void addPoints(String team, int points) {
        if (!teamScores.containsKey(team)) {
            teamScores.put(team, points);
            return;
        }
        int oldScore = teamScores.get(team);
        teamScores.put(team, oldScore + points);
    }
    
    /**
     * Gets the score for a given participant. Defaults to 0 if no points have been added
     * for the participant. 
     * @param participantUUID the participant to get the score for
     * @return the participant's score
     */
    public int getScore(UUID participantUUID) {
        if (!participantScores.containsKey(participantUUID)) {
            return 0;
        }
        return participantScores.get(participantUUID);
    }
    
    /**
     * Gets the score for a given team. Defaults to 0 if the no points have been added
     * for the team.
     * @param team The team to get the score of
     * @return the team's score
     */
    public int getScore(String team) {
        if (!teamScores.containsKey(team)) {
            return 0;
        }
        return teamScores.get(team);
    }
}
