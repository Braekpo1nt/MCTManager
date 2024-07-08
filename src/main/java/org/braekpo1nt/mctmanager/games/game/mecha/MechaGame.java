package org.braekpo1nt.mctmanager.games.game.mecha;

import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.game.interfaces.MCTGame;
import org.braekpo1nt.mctmanager.games.game.mecha.states.MechaState;
import org.braekpo1nt.mctmanager.games.game.mecha.states.StartingState;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * The context for the state pattern
 */
public class MechaGame implements MCTGame {
    
    private MechaState state;
    public List<Player> participants;
    
    @Override
    public GameType getType() {
        return GameType.MECHA;
    }
    
    @Override
    public void start(List<Player> newParticipants, List<Player> newAdmins) {
        state = new StartingState(this);
    }
    
    @Override
    public void stop() {
        
    }
    
    @Override
    public void onParticipantJoin(Player participant) {
        
    }
    
    @Override
    public void onParticipantQuit(Player participant) {
        
    }
    
    @Override
    public void onAdminJoin(Player admin) {
        
    }
    
    @Override
    public void onAdminQuit(Player admin) {
        
    }
    
    @Override
    public @NotNull String getBaseTitle() {
        return "";
    }
    
    @Override
    public void setTitle(@NotNull String title) {
        
    }
}
