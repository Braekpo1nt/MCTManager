package org.braekpo1nt.mctmanager.games.experimental;


import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.participant.*;
import org.bukkit.GameMode;

import java.util.UUID;

public abstract class GameStateBase<P extends ParticipantData, T extends ScoredTeamData<P>, QP extends QuitDataBase, QT extends QuitDataBase, C extends GameBase<P, T, QP, QT>> {
    
    protected final C context;
    
    public GameStateBase(C context) {
        this.context = context;
    }
    
    public abstract void cleanup();
    
    // join start
    public void onTeamJoin(Team newTeam) {
        if (context.getTeams().containsKey(newTeam.getTeamId())) {
            return;
        }
        QT quitTeam = context.getTeamQuitDatas().remove(newTeam.getTeamId());
        if (quitTeam != null) {
            T team = context.createTeam(newTeam, quitTeam);
            context.getTeams().put(team.getTeamId(), team);
            onTeamRejoin(team);
        } else {
            T team = context.createTeam(newTeam);
            context.getTeams().put(team.getTeamId(), team);
            onNewTeamJoin(team);
        }
    }
    
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
    
    public void onParticipantJoin(Participant newParticipant) {
        T team = context.getTeams().get(newParticipant.getTeamId());
        QP quitData = context.getQuitDatas().get(newParticipant.getUniqueId());
        P participant;
        if (quitData != null) {
            participant = context.createParticipant(newParticipant, quitData);
            _onParticipantRejoin(participant, team);
        } else {
            participant = context.createParticipant(newParticipant);
            _onNewParticipantJoin(participant, team);
        }
        updateSidebar(participant, team);
    }
    
    protected void updateSidebar(P participant, T team) {
        context.getSidebar().updateLine(participant.getUniqueId(), "title", context.getTitle());
        context.displayScore(participant);
        context.displayScore(team);
    }
    
    protected void initializeParticipant(P participant, T team) {
        team.addParticipant(participant);
        context.getSidebar().addPlayer(participant);
        context.getUiManagers().forEach(uiManager -> uiManager.showPlayer(participant));
        context.getParticipants().put(participant.getUniqueId(), participant);
        participant.setGameMode(GameMode.ADVENTURE);
        ParticipantInitializer.clearStatusEffects(participant);
        ParticipantInitializer.clearInventory(participant);
        ParticipantInitializer.resetHealthAndHunger(participant);
    }
    
    protected void _onParticipantRejoin(P participant, T team) {
        initializeParticipant(participant, team);
        onParticipantRejoin(participant, team);
    }
    
    /**
     * 
     * @param participant the participant who is rejoining
     * @param team the participant's team
     */
    protected abstract void onParticipantRejoin(P participant, T team);
    
    protected void _onNewParticipantJoin(P participant, T team) {
        initializeParticipant(participant, team);
        onNewParticipantJoin(participant, team);
    }
    
    /**
     * 
     * @param participant the participant who is joining for the first time
     * @param team the participant's team
     */
    protected abstract void onNewParticipantJoin(P participant, T team);
    // join end
    
    public void onParticipantQuit(UUID participantUUID) {
        P participant = context.getParticipants().remove(participantUUID);
        if (participant == null) {
            return;
        }
        context.getQuitDatas().put(participant.getUniqueId(), context.getQuitData(participant));
        T team = context.getTeams().get(participant.getTeamId());
        _resetParticipant(participant, team);
        onParticipantQuit(participant, team);
    }
    
    protected void _resetParticipant(P participant, T team) {
        team.removeParticipant(participant.getUniqueId());
        ParticipantInitializer.clearInventory(participant);
        ParticipantInitializer.resetHealthAndHunger(participant);
        ParticipantInitializer.clearStatusEffects(participant);
        participant.setGameMode(GameMode.SPECTATOR);
        context.getSidebar().removePlayer(participant);
        context.getUiManagers().forEach(uiManager -> uiManager.hidePlayer(participant));
    }
    
    /**
     * 
     * @param participant the participant who quit. 
     *                    (Will not be found in {@link GameBase#getParticipants()}.)
     * @param team the participant's old team.
     *             (The participant is no longer found in that team's 
     *             {@link ScoredTeamData#getParticipants()}).
     */
    protected abstract void onParticipantQuit(P participant, T team);
    
    public void onTeamQuit(String teamId) {
        T team = context.getTeams().get(teamId);
        if (team.size() > 0) {
            return;
        }
        context.getTeams().remove(team.getTeamId());
        context.getTeamQuitDatas().put(team.getTeamId(), context.getQuitData(team));
        onTeamQuit(team);
    }
    
    /**
     * <p>React to a team quitting (all its members have quit).</p>
     * @param team the team that has quit. Has no more members. 
     *             (Will not be found in {@link GameBase#getTeams()}.)
     */
    protected abstract void onTeamQuit(T team);
}
