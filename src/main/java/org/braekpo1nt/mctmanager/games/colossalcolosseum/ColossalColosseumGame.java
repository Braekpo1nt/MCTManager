package org.braekpo1nt.mctmanager.games.colossalcolosseum;

import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.enums.MCTGames;
import org.braekpo1nt.mctmanager.games.interfaces.MCTGame;
import org.bukkit.entity.Player;

import java.util.List;

public class ColossalColosseumGame implements MCTGame {
    
    private final Main plugin;
    private final GameManager gameManager;
    
    public ColossalColosseumGame(Main plugin, GameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
    }
    
    @Override
    public MCTGames getType() {
        return MCTGames.COLOSSAL_COLOSSEUM;
    }
    
    /**
     * Start the game with the first and second place teams, and the spectators. 
     * @param newFirstPlaceParticipants The participants in the first place team
     * @param newSecondPlaceParticipants The participants in the second place team
     * @param newSpectators The participants who are third place and on, who should spectate the game
     */
    public void start(List<Player> newFirstPlaceParticipants, List<Player> newSecondPlaceParticipants, List<Player> newSpectators) {
        
    }
    
    /**
     * Do not use this method. Instead, use {@link ColossalColosseumGame#start(List, List, List)}
     * @param newParticipants The participants
     */
    @Override
    public void start(List<Player> newParticipants) {
        throw new UnsupportedOperationException("ColossalColosseumGame is a special case, because it is the final game. Please use the overload method, start(List<Player> newFirstPlaceParticipants, List<Player> newSecondPlaceParticipants, List<Player> newSpectators).");
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
