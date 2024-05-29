package org.braekpo1nt.mctmanager.games.gamestate;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.config.ConfigUtils;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigException;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigIOException;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigInvalidException;
import org.braekpo1nt.mctmanager.config.validation.Validator;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class GameStateController {
    
    private final File gameStateFile;
    
    public GameStateController(File configDirectory) {
        this.gameStateFile = new File(configDirectory, "gameState.json");
    }
    
    /**
     * Gets the GameState from storage
     * @return the GameState
     * @throws ConfigException if there is an IO error getting the GameState
     */
    public @NotNull GameState getGameState() throws ConfigException {
        GameStateDTO gameStateDTO = loadConfigDTO(gameStateFile);
        gameStateDTO.validate(new Validator("gameState"));
        return gameStateDTO.toGameState();
    }
    
    /**
     * Load the GameStateDTO from the given file
     * @param file the file to load the GameState from
     * @return The GameStateDTO stored in the file
     * @throws ConfigInvalidException if there is a problem parsing the JSON into a GameStateDTO
     * @throws ConfigIOException if there is an IO problem getting the GameStateDTO
     */
    public @NotNull GameStateDTO loadConfigDTO(@NotNull File file) throws ConfigInvalidException, ConfigIOException {
        try {
            if (!file.exists()) {
                return new GameStateDTO(new HashMap<>(), new HashMap<>(), new ArrayList<>());
            }
        } catch (SecurityException e) {
            throw new ConfigIOException(String.format("Security exception while trying to read %s", file), e);
        }
        try {
            Reader reader = new FileReader(file);
            GameStateDTO configDTO = ConfigUtils.GSON.fromJson(reader, GameStateDTO.class);
            reader.close();
            return configDTO;
        } catch (IOException | JsonIOException e) {
            throw new ConfigIOException(String.format("Error while reading %s", file), e);
        } catch (JsonSyntaxException e) {
            throw new ConfigInvalidException(String.format("Error parsing %s", file), e);
        }
    }
    
    /**
     * Save the given GameState to its file. Note that this does not validate the GameStateDTO
     * @param gameState the GameState to save
     * @throws org.braekpo1nt.mctmanager.config.exceptions.ConfigIOException if there is an IO error while saving the GameState file
     */
    public void saveGameState(@NotNull GameState gameState) throws ConfigIOException {
        GameStateDTO gameStateDTO = GameStateDTO.fromGameState(gameState);
        saveConfigDTO(gameStateDTO, gameStateFile);
    }
    
    /**
     * Saves the given Config to storage
     * @param configDTO the config to save
     * @param configFile the file to save the config to
     * @throws ConfigIOException if there is an IO error saving the config to the file system
     */
    public void saveConfigDTO(@NotNull GameStateDTO configDTO, @NotNull File configFile) throws ConfigIOException {
        try {
            if (!configFile.exists()) {
                if (!configFile.mkdirs()) {
                    throw new ConfigIOException(String.format("Unable to create directories for %s", configFile));
                }
                if (configFile.createNewFile()) {
                    throw new ConfigIOException(String.format("Unable to create config file %s", configFile));
                }
            }
        } catch (SecurityException e) {
            throw new ConfigIOException(String.format("Security exception while trying to save config to %s", configFile), e);
        } catch (IOException e) {
            throw new ConfigIOException(String.format("Error while trying to create %s", configFile), e);
        }
        
        try {
            Writer writer = new FileWriter(configFile, false);
            Main.GSON.toJson(configDTO, writer);
            writer.flush();
            writer.close();
        } catch (JsonIOException | IOException e) {
            throw new ConfigIOException(String.format("Error while writing %s", configFile), e);
        }
    }
    
}
