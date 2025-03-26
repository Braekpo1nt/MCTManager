package org.braekpo1nt.mctmanager.games.experimental;


import org.braekpo1nt.mctmanager.participant.*;

public abstract class GameStateBase<P extends ParticipantData, T extends ScoredTeamData<P>, C extends GameBase<P, T>> {
    
    protected final C context;
    
    public GameStateBase(C context) {
        this.context = context;
    }
    
    public abstract void cleanup();
    
    public final void _onTeamJoin(Team newTeam) {
        if (context.getTeams().containsKey(newTeam.getTeamId())) {
            return;
        }
        T quitTeam = context.getQuitTeams().remove(newTeam.getTeamId());
        T team;
        if (quitTeam != null) {
            reInitializeTeam(quitTeam);
            team = quitTeam;
        } else {
            team = context.createTeam(newTeam);
            initializeTeam(team);
        }
        context.getTeams().put(team.getTeamId(), team);
    }
    
    /**
     * <p>Called when a new team joins.</p>
     * <p>Initialize team</p>
     * @param team the team
     */
    protected abstract void initializeTeam(T team);
    
    /**
     * <p>Called when a previously quit team needs to be re-initialized</p>
     * @param team the team who was previously quit, and has rejoined
     */
    protected abstract void reInitializeTeam(T team);
    
    public void _onParticipantJoin(Participant participant, Team team) {
        
    }
}
