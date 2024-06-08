package org.braekpo1nt.mctmanager.games.gamestate;

import com.google.gson.Gson;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.config.ConfigController;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigException;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigIOException;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigInvalidException;
import org.braekpo1nt.mctmanager.config.validation.Validator;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class GameStateController extends ConfigController<GameStateDTO> {
    
    private final File gameStateFile;
    
    public GameStateController(File configDirectory) {
        this.gameStateFile = new File(configDirectory, "gameState.json");
    }
    
    @Override
    protected @NotNull Gson getGson() {
        return Main.GSON_PRETTY;
    }
    
    /**
     * Gets the GameState from storage
     * @return the GameState
     * @throws ConfigException if there is an IO error getting the GameState
     */
    public @NotNull GameState getGameState() throws ConfigException {
        GameStateDTO gameStateDTO = loadConfigDTO(gameStateFile, GameStateDTO.class);
        gameStateDTO.validate(new Validator("gameState"));
        return gameStateDTO.toGameState();
    }
    
    /**
     * Saves the given GameState to storage
     * @param gameState the GameState to save
     */
    public void saveGameState(@NotNull GameState gameState) {
        GameStateDTO gameStateDTO = GameStateDTO.fromGameState(gameState);
        saveConfigDTO(gameStateDTO, gameStateFile);
    }
    
    @Override
    public @NotNull GameStateDTO loadConfigDTO(@NotNull File configFile, @NotNull Class<GameStateDTO> configType) throws ConfigInvalidException, ConfigIOException {
        if (!configFile.exists()) {
            return new GameStateDTO();
        }
        return super.loadConfigDTO(configFile, configType);
    }
}
