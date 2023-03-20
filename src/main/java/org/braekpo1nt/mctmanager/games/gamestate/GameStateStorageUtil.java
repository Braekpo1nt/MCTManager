package org.braekpo1nt.mctmanager.games.gamestate;

import com.google.gson.Gson;
import org.braekpo1nt.mctmanager.Main;
import org.bukkit.Bukkit;

import java.io.*;
import java.util.Arrays;
import java.util.List;

/**
 * Handles the CRUD operations for storing GameState objects
 */
public class GameStateStorageUtil {
    
    private static final String GAME_STATE_FILE_NAME = "gameState.json";
    private final File gameStateDirectory;
    private GameState gameState = new GameState();
    
    public GameStateStorageUtil(Main plugin) {
        this.gameStateDirectory = plugin.getDataFolder().getAbsoluteFile();
    }
    
    /**
     * Save the GameState to storage
     * @throws IOException if there is a problem
     * - creating a new game state file
     * - writing to the game state file
     * - converting the game state to json
     */
    public void saveGameState() throws IOException {
        Gson gson = new Gson();
        File gameStateFile = getGameStateFile();
        Writer writer = new FileWriter(gameStateFile, false);
        gson.toJson(this.gameState, writer);
        writer.flush();
        writer.close();
        Bukkit.getLogger().info("[MCTManager] Saved game state.");
    }
    
    /**
     * Load the GameState from storage
     * @throws IOException if there is a problem 
     * - creating a new game state file
     * - reading the existing game state file
     * - parsing the game state from json
     */
    public void loadGameState() throws IOException {
        Gson gson = new Gson();
        File gameStateFile = getGameStateFile();
        Reader reader = new FileReader(gameStateFile);
        GameState newGameState = gson.fromJson(reader, GameState.class);
        reader.close();
        if (newGameState == null) {
            newGameState = new GameState();
        }
        this.gameState = newGameState;
        Bukkit.getLogger().info("[MCTManager] Loaded game state.");
    }
    
    /**
     * Get the game state file from the given game state directory. Creates the file
     * if one doesn't already exist.
     * @return The game state file
     * @throws IOException if there is an error creating a new game state file
     */
    private File getGameStateFile() throws IOException {
        File gameStateFile = new File(gameStateDirectory.getAbsolutePath(), this.GAME_STATE_FILE_NAME);
        if (!gameStateFile.exists()) {
            if (!gameStateDirectory.exists()) {
                gameStateDirectory.mkdirs();
            }
            gameStateFile.createNewFile();
        }
        return gameStateFile;
    }
    
    /**
     * Add a team to the game state.
     * @param teamName The internal name of the team.
     * @param teamDisplayName The display name of the team.
     * @return True if the team was created successfully, false if a team with that
     * teamName already exists in the game state. 
     * @throws IOException If there is an error saving the game state while adding a new team.
     */
    public boolean addTeam(String teamName, String teamDisplayName) throws IOException {
        if (gameState.hasTeam(teamName)) {
            return false;
        }
        gameState.addTeam(teamName, teamDisplayName);
        saveGameState();
        return true;
    }
}
