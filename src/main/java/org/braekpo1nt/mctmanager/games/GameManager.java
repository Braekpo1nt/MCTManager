package org.braekpo1nt.mctmanager.games;

import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.footrace.FootRaceGame;
import org.braekpo1nt.mctmanager.games.gamestate.GameStateStorageUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Responsible for overall game management. 
 * Creating new game instances, starting/stopping games, and handling game events.
 */
public class GameManager {
    
    private MCTGame activeGame = null;
    private final FootRaceGame footRaceGame;
    private final GameStateStorageUtil gameStateStorageUtil;
    
    public GameManager(Main plugin) {
        gameStateStorageUtil = new GameStateStorageUtil(plugin);
        this.footRaceGame = new FootRaceGame(plugin);
    }
    
    public void loadGameState() throws IOException {
        gameStateStorageUtil.loadGameState();
    }
    
    public void saveGameState() throws IOException, IllegalStateException {
        gameStateStorageUtil.saveGameState();
    }
    
    public void startGame(String gameName, Player sender) {
    
        if (activeGame != null) {
            sender.sendMessage("There is already a game running. You must stop the game before you start a new one.");
            return;
        }
        
        List<Player> participants = Arrays.asList(Bukkit.getPlayer("Braekpo1nt"));
        
        switch (gameName) {
            case "foot-race":
                footRaceGame.start(participants);
                activeGame = footRaceGame;
                break;
//            case "mecha":
//                player.sendMessage("2");
//                break;
//            case "bedwars":
//                player.sendMessage("3");
//                break;
//            case "capture-the-flag":
//                player.sendMessage("4");
//                break;
//            case "dodgeball":
//                player.sendMessage("5");
//                break;
//            case "spleef":
//                player.sendMessage("6");
//                break;
//            case "parkour-pathway":
//                player.sendMessage("7");
//                break;
            default:
                sender.sendMessage("Unknown game: " + gameName);
                break;
        }
    }

    /**
     * If a game is currently going on, stops the game
     */
    public void stopGame(Player sender) {
        if (activeGame == null) {
            sender.sendMessage("No game is running.");
            return;
        }
        ((FootRaceGame) activeGame).stop();
        activeGame = null;
    }
}
