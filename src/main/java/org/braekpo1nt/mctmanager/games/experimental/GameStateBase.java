package org.braekpo1nt.mctmanager.games.experimental;


import org.braekpo1nt.mctmanager.participant.*;

public interface GameStateBase<P extends ParticipantData, T extends ScoredTeamData<P>> {
    
    void cleanup();
    
    // join start
    /**
     * 
     * @param team the team that is rejoining
     */
    void onTeamRejoin(T team);
    
    /**
     * 
     * @param team the team that is joining for the first time
     */
    void onNewTeamJoin(T team);
    
    /**
     * Called when a participant rejoins the game after having quit
     * @param participant the participant who is rejoining
     * @param team the participant's team
     */
    void onParticipantRejoin(P participant, T team);
    
    /**
     * Called when a participant joins the game for the first time
     * @param participant the participant who is joining for the first time
     * @param team the participant's team
     */
    void onNewParticipantJoin(P participant, T team);
    
    // join end
    
    /**
     * 
     * @param participant the participant who quit. 
     *                    (Will not be found in {@link GameBase#getParticipants()}.)
     * @param team the participant's old team.
     *             (The participant is no longer found in that team's 
     *             {@link ScoredTeamData#getParticipants()}).
     */
    void onParticipantQuit(P participant, T team);
    
    /**
     * <p>React to a team quitting (all its members have quit).</p>
     * @param team the team that has quit. Has no more members. 
     *             (Will not be found in {@link GameBase#getTeams()}.)
     */
    void onTeamQuit(T team);
}
