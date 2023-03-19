package org.braekpo1nt.mctmanager.games.gamestate;

import com.google.gson.Gson;
import org.braekpo1nt.mctmanager.Main;
import org.bukkit.Bukkit;

import javax.print.DocFlavor;
import java.io.*;
import java.util.UUID;

/**
 * Handles the CRUD operations for storing GameState objects
 */
public class GameStateStorageUtil {
    
    private static final String GAME_STATE_FILE_NAME = "gameState.json";
    private GameState gameState = new GameState();
    
    //TODO: this should be addPlayer and addTeam not createGameState
    public GameState createGameState() {
        this.gameState = new GameState();
        return this.gameState;
    }
    
    /**
     * Load the GameState from memory
     * @return
     * @throws IOException if there is a problem 
     * - creating a new game state file
     * - reading the existing game state file
     * - parsing the game state from json
     */
    public void loadGameState(Main plugin) throws IOException {
        File gameStateFile = getGameStateFile(plugin.getDataFolder().getAbsoluteFile());
        Reader reader = new FileReader(gameStateFile);
        Gson gson = new Gson();
        GameState newGameState = gson.fromJson(reader, GameState.class);
        this.gameState = newGameState;
        Bukkit.getLogger().info("Successfully loaded same state.");
    }
    
    /**
     * Get the game state file from the given game state directory. Creates the file
     * if one doesn't already exist. 
     * @param gameStateDirectory
     * @return The game state file
     * @throws IOException if there is an error creating a new game state file
     */
    private File getGameStateFile(File gameStateDirectory) throws IOException {
        if (!gameStateDirectory.exists()) {
            gameStateDirectory.mkdirs();
        }
        File gameStateFile = new File(gameStateDirectory.getAbsolutePath(), this.GAME_STATE_FILE_NAME);
        if (!gameStateFile.exists()) {
            gameStateFile.createNewFile();
        }
        return gameStateFile;
    }
    
}
