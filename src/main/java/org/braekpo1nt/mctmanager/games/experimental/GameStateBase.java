package org.braekpo1nt.mctmanager.games.experimental;


import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.participant.*;
import org.bukkit.GameMode;

import java.util.UUID;

public abstract class GameStateBase<P extends ParticipantData, T extends ScoredTeamData<P>, QP extends QuitDataBase, QT extends QuitDataBase> {
    
    protected abstract GameBase<P, T, QP, QT> getContext();
    
    public abstract void cleanup();
    
    // join start
    /**
     * 
     * @param team the team that is rejoining
     */
    protected abstract void onTeamRejoin(T team);
    
    /**
     * 
     * @param team the team that is joining for the first time
     */
    protected abstract void onNewTeamJoin(T team);
    
    /**
     *
     * @param participant the participant who is rejoining
     * @param team the participant's team
     */
    protected abstract void onParticipantRejoin(P participant, T team);
    
    /**
     * 
     * @param participant the participant who is joining for the first time
     * @param team the participant's team
     */
    protected abstract void onNewParticipantJoin(P participant, T team);
    // join end
    
    /**
     * 
     * @param participant the participant who quit. 
     *                    (Will not be found in {@link GameBase#getParticipants()}.)
     * @param team the participant's old team.
     *             (The participant is no longer found in that team's 
     *             {@link ScoredTeamData#getParticipants()}).
     */
    protected abstract void onParticipantQuit(P participant, T team);
    
    /**
     * <p>React to a team quitting (all its members have quit).</p>
     * @param team the team that has quit. Has no more members. 
     *             (Will not be found in {@link GameBase#getTeams()}.)
     */
    protected abstract void onTeamQuit(T team);
}
