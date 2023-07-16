package org.braekpo1nt.mctmanager.games.colossalcolosseum;

import org.braekpo1nt.mctmanager.games.enums.MCTGames;
import org.braekpo1nt.mctmanager.games.interfaces.MCTGame;
import org.bukkit.entity.Player;

import java.util.List;

public class ColossalColosseumGame implements MCTGame {
    
    @Override
    public MCTGames getType() {
        return MCTGames.COLOSSAL_COLOSSEUM;
    }
    
    @Override
    public void start(List<Player> newParticipants) {
        
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
}
