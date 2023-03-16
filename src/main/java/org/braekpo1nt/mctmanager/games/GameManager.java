package org.braekpo1nt.mctmanager.games;

import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.footrace.FootRaceGame;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

/**
 * Responsible for overall game management. 
 * Creating new game instances, starting/stopping games, and handling game events.
 */
public class GameManager {
    
    private final List<Player> participants;
    private final FootRaceGame footRaceGame;
    
    public GameManager(Main plugin) {
        participants = Arrays.asList(Bukkit.getPlayer("Braekpo1nt"));
        this.footRaceGame = new FootRaceGame(plugin, participants);
    }
    
    public void startGame(String gameName, Player sender) {
        
        switch (gameName) {
            case "foot-race":
                footRaceGame.start();
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
    public void stopGame() {
        if (footRaceGame.isGameActive()) {
            footRaceGame.stop();
        }
    }
}
