package org.braekpo1nt.mctmanager.games.experimental;


import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.braekpo1nt.mctmanager.participant.*;
import org.bukkit.GameMode;

import java.util.UUID;

public abstract class GameStateBase<P extends ParticipantData, T extends ScoredTeamData<P>, QP extends QuitDataBase, QT extends QuitDataBase> {
    
    protected abstract GameBase<P, T, QP, QT> getContext();
    
    public abstract void cleanup();
    
    // join start
    public void onTeamJoin(Team newTeam) {
        if (getContext().getTeams().containsKey(newTeam.getTeamId())) {
            return;
        }
        QT quitTeam = getContext().getTeamQuitDatas().remove(newTeam.getTeamId());
        if (quitTeam != null) {
            T team = getContext().createTeam(newTeam, quitTeam);
            getContext().getTeams().put(team.getTeamId(), team);
            onTeamRejoin(team);
        } else {
            T team = getContext().createTeam(newTeam);
            getContext().getTeams().put(team.getTeamId(), team);
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
        T team = getContext().getTeams().get(newParticipant.getTeamId());
        QP quitData = getContext().getQuitDatas().get(newParticipant.getUniqueId());
        P participant;
        if (quitData != null) {
            participant = getContext().createParticipant(newParticipant, quitData);
            _onParticipantRejoin(participant, team);
        } else {
            participant = getContext().createParticipant(newParticipant);
            _onNewParticipantJoin(participant, team);
        }
        updateSidebar(participant, team);
    }
    
    protected void updateSidebar(P participant, T team) {
        getContext().getSidebar().updateLine(participant.getUniqueId(), "title", getContext().getTitle());
        getContext().displayScore(participant);
        getContext().displayScore(team);
    }
    
    protected void initializeParticipant(P participant, T team) {
        team.addParticipant(participant);
        getContext().getSidebar().addPlayer(participant);
        getContext().getUiManagers().forEach(uiManager -> uiManager.showPlayer(participant));
        getContext().getParticipants().put(participant.getUniqueId(), participant);
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
        P participant = getContext().getParticipants().remove(participantUUID);
        if (participant == null) {
            return;
        }
        getContext().getQuitDatas().put(participant.getUniqueId(), getContext().getQuitData(participant));
        T team = getContext().getTeams().get(participant.getTeamId());
        _resetParticipant(participant, team);
        onParticipantQuit(participant, team);
    }
    
    protected void _resetParticipant(P participant, T team) {
        team.removeParticipant(participant.getUniqueId());
        ParticipantInitializer.clearInventory(participant);
        ParticipantInitializer.resetHealthAndHunger(participant);
        ParticipantInitializer.clearStatusEffects(participant);
        participant.setGameMode(GameMode.SPECTATOR);
        getContext().getSidebar().removePlayer(participant);
        getContext().getUiManagers().forEach(uiManager -> uiManager.hidePlayer(participant));
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
        T team = getContext().getTeams().get(teamId);
        if (team.size() > 0) {
            return;
        }
        getContext().getTeams().remove(team.getTeamId());
        getContext().getTeamQuitDatas().put(team.getTeamId(), getContext().getQuitData(team));
        onTeamQuit(team);
    }
    
    /**
     * <p>React to a team quitting (all its members have quit).</p>
     * @param team the team that has quit. Has no more members. 
     *             (Will not be found in {@link GameBase#getTeams()}.)
     */
    protected abstract void onTeamQuit(T team);
}
