package org.braekpo1nt.mctmanager.games.game.farmrush;

import lombok.Data;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigIOException;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigInvalidException;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.game.farmrush.states.FarmRushState;
import org.braekpo1nt.mctmanager.games.game.interfaces.Configurable;
import org.braekpo1nt.mctmanager.games.game.interfaces.MCTGame;
import org.braekpo1nt.mctmanager.ui.sidebar.Headerable;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Data
public class FarmRushGame implements MCTGame, Configurable, Headerable, Listener {
    
    private @Nullable FarmRushState state;
    
    @Override
    public void loadConfig() throws ConfigIOException, ConfigInvalidException {
        
    }
    
    @Override
    public GameType getType() {
        return null;
    }
    
    @Override
    public void start(List<Player> newParticipants, List<Player> newAdmins) {
        
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
    public @NotNull Component getBaseTitle() {
        return null;
    }
    
    @Override
    public void setTitle(@NotNull Component title) {
        
    }
    
    @Override
    public void updatePersonalScore(Player participant, Component contents) {
        
    }
    
    @Override
    public void updateTeamScore(Player participant, Component contents) {
        
    }
}
