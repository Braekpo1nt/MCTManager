package org.braekpo1nt.mctmanager.games.colossalcolosseum;

import com.onarandombox.MultiverseCore.utils.AnchorManager;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.GameManager;
import org.braekpo1nt.mctmanager.games.enums.MCTGames;
import org.braekpo1nt.mctmanager.games.interfaces.MCTGame;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ColossalColosseumGame implements MCTGame {
    
    private final Main plugin;
    private final GameManager gameManager;
    private final Location firstPlaceSpawn;
    private final Location secondPlaceSpawn;
    private final Location spectatorSpawn;
    private List<Player> firstPlaceParticipants = new ArrayList<>();
    private List<Player> secondPlaceParticipants = new ArrayList<>();
    private List<Player> spectators = new ArrayList<>();
    private List<ColossalColosseumRound> rounds = new ArrayList<>();
    private int currentRoundIndex = 0;
    
    public ColossalColosseumGame(Main plugin, GameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
        AnchorManager anchorManager = Main.multiverseCore.getAnchorManager();
        this.firstPlaceSpawn = anchorManager.getAnchorLocation("cc-first-place-spawn");
        this.secondPlaceSpawn = anchorManager.getAnchorLocation("cc-second-place-spawn");
        this.spectatorSpawn = anchorManager.getAnchorLocation("cc-spectator-spawn");
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
        firstPlaceParticipants = new ArrayList<>(newFirstPlaceParticipants.size());
        secondPlaceParticipants = new ArrayList<>(newSecondPlaceParticipants.size());
        spectators = new ArrayList<>(newSpectators.size());
        rounds = new ArrayList<>(3);
        rounds.add(new ColossalColosseumRound(plugin, gameManager, this));
        rounds.add(new ColossalColosseumRound(plugin, gameManager, this));
        rounds.add(new ColossalColosseumRound(plugin, gameManager, this));
        currentRoundIndex = 0;
        for (Player first : newFirstPlaceParticipants) {
            initializeFirstPlaceParticipant(first);
        }
        for (Player second : newSecondPlaceParticipants) {
            initializeSecondPlaceParticipant(second);
        }
        for (Player spectator : newSpectators) {
            initializeSpectator(spectator);
        }
        setupTeamOptions();
        startNextRound();
        Bukkit.getLogger().info("Started Colossal Colosseum");
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
        cancelAllTasks();
        if (currentRoundIndex < rounds.size()) {
            ColossalColosseumRound currentRound = rounds.get(currentRoundIndex);
            currentRound.stop();
        }
        rounds.clear();
        for (Player participant : firstPlaceParticipants) {
            
        }
        firstPlaceParticipants.clear();
        for (Player participant : secondPlaceParticipants) {
        
        }
        secondPlaceParticipants.clear();
        for (Player participant : spectators) {
        
        }
        spectators.clear();
        gameManager.gameIsOver();
        Bukkit.getLogger().info("Stopping Colossal Colosseum");
    }
    
    @Override
    public void onParticipantJoin(Player participant) {
        
    }
    
    @Override
    public void onParticipantQuit(Player participant) {
        
    }
}
