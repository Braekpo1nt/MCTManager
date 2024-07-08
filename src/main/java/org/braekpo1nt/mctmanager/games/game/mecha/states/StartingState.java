package org.braekpo1nt.mctmanager.games.game.mecha.states;

import lombok.AllArgsConstructor;
import org.braekpo1nt.mctmanager.games.game.mecha.MechaGame;
import org.braekpo1nt.mctmanager.games.utils.ParticipantInitializer;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

@AllArgsConstructor
public class StartingState implements MechaState {
    private MechaGame context;
    
    @Override
    public void onParticipantJoin(Player participant) {
        
    }
    
    @Override
    public void onParticipantQuit(Player participant) {
        
    }
    
    public void initializeParticipant(Player participant) {
        context.getParticipants().add(participant);
        context.getLivingPlayers().add(participant.getUniqueId());
        String teamId = context.getGameManager().getTeamName(participant.getUniqueId());
        context.getLivingMembers().putIfAbsent(teamId, 0);
        int oldAliveCount = context.getLivingMembers().get(teamId);
        context.getLivingMembers().put(teamId, oldAliveCount + 1);
        context.getSidebar().addPlayer(participant);
        context.getTopbar().showPlayer(participant);
        context.getTopbar().linkToTeam(participant.getUniqueId(), teamId);
        context.updateAliveCount(teamId);
        context.initializeKillCount(participant);
        participant.setGameMode(GameMode.ADVENTURE);
        participant.getInventory().clear();
        ParticipantInitializer.resetHealthAndHunger(participant);
        ParticipantInitializer.clearStatusEffects(participant);
    }
    
    public void resetParticipant(Player participant) {
        participant.getInventory().clear();
        ParticipantInitializer.clearStatusEffects(participant);
        ParticipantInitializer.resetHealthAndHunger(participant);
        context.getSidebar().removePlayer(participant.getUniqueId());
        context.getTopbar().hidePlayer(participant.getUniqueId());
    }
}
