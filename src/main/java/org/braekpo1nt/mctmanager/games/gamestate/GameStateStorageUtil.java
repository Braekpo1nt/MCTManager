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
    private GameState gameState = new GameState();
    
    //TODO: addPlayer and addTeam
    
    /**
     * Save the GameState to storage
     * @param plugin The main plugin instance
     * @throws IOException if there is a problem
     * - creating a new game state file
     * - writing to the game state file
     * - converting the game state to json
     */
    public void saveGameState(Main plugin) throws IOException {
        Gson gson = new Gson();
        File gameStateFile = getGameStateFile(plugin.getDataFolder().getAbsoluteFile());
        Writer writer = new FileWriter(gameStateFile, false);
        gson.toJson(this.gameState, writer);
        writer.flush();
        writer.close();
        Bukkit.getLogger().info("Saved game state " + gameStateFile.getAbsolutePath().toString());
    }
    
    /**
     * Load the GameState from storage
     * @param plugin The main plugin instance
     * @throws IOException if there is a problem 
     * - creating a new game state file
     * - reading the existing game state file
     * - parsing the game state from json
     */
    public void loadGameState(Main plugin) throws IOException {
        Gson gson = new Gson();
        File gameStateFile = getGameStateFile(plugin.getDataFolder().getAbsoluteFile());
        Reader reader = new FileReader(gameStateFile);
        GameState newGameState = gson.fromJson(reader, GameState.class);
        reader.close();
        this.gameState = newGameState;
        Bukkit.getLogger().info("Loaded game state. \n" + gameState.toString());
    }
    
    /**
     * Get the game state file from the given game state directory. Creates the file
     * if one doesn't already exist. 
     * @param gameStateDirectory
     * @return The game state file
     * @throws IOException if there is an error creating a new game state file
     */
    private File getGameStateFile(File gameStateDirectory) throws IOException {
        File gameStateFile = new File(gameStateDirectory.getAbsolutePath(), this.GAME_STATE_FILE_NAME);
        if (!gameStateFile.exists()) {
            if (!gameStateDirectory.exists()) {
                gameStateDirectory.mkdirs();
            }
            gameStateFile.createNewFile();
        }
        return gameStateFile;
    }
    
}
