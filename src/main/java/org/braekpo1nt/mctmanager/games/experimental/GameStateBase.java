package org.braekpo1nt.mctmanager.games.experimental;


import org.braekpo1nt.mctmanager.participant.*;

public abstract class GameStateBase<P extends ParticipantData, T extends ScoredTeamData<P>, QP extends QuitDataBase, QT extends QuitDataBase, C extends GameBase<P, T, QP, QT>> {
    
    protected final C context;
    
    public GameStateBase(C context) {
        this.context = context;
    }
    
    public abstract void cleanup();
    
    public final void _onTeamJoin(Team newTeam) {
        if (context.getTeams().containsKey(newTeam.getTeamId())) {
            return;
        }
        QT quitData = context.getTeamQuitDatas().remove(newTeam.getTeamId());
        T team;
        if (quitData != null) {
            team = context.createTeam(newTeam, quitData);
        } else {
            team = context.createTeam(newTeam);
        }
        context.getTeams().put(team.getTeamId(), team);
        initializeTeam(team);
    }
    
    /**
     * <p>Called when the first player of a {@link T} team joins.</p>
     * <p>Initialize team</p>
     * @param team the team
     */
    protected abstract void initializeTeam(T team);
    
    public void _onParticipantJoin(Participant participant, Team team) {
        
    }
}
