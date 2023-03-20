package org.braekpo1nt.mctmanager.games;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.footrace.FootRaceGame;
import org.braekpo1nt.mctmanager.games.gamestate.GameStateStorageUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

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
    
    /**
     * Add a team to the game.
     * @param teamName The internal name of the team.
     * @param teamDisplayName The display name of the team.
     * @return True if the team was successfully created. False if the team already exists.
     * @throws IOException If there was a problem saving the game state with the new team.
     */
    public boolean addTeam(String teamName, String teamDisplayName) throws IOException {
        boolean teamExistsAlready = !gameStateStorageUtil.addTeam(teamName, teamDisplayName);
        if (teamExistsAlready) {
            return false;
        }
        boolean minecraftTeamExistsAlready = !createMinecraftTeam(teamName, teamDisplayName);
        if (minecraftTeamExistsAlready) {
            return false;
        }
        return true;
    }
    
    /**
     * Create a minecraft scoreboard team with the given teamName and displayName.
     * @param teamName The internal team name.
     * @param teamDisplayName The display name of the team.
     * @return True if the team was successfully created, false if the team already exists
     */
    private boolean createMinecraftTeam(String teamName, String teamDisplayName) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        if (scoreboard.getTeam(teamName) != null) {
            return false;
        }
        Team newTeam = scoreboard.registerNewTeam(teamName);
        newTeam.displayName(Component.text(teamDisplayName));
        return true;
    }
}
