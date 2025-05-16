package org.braekpo1nt.mctmanager.games.gamestate.preset;

import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.FailureCommandResult;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.SuccessCommandResult;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigException;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigIOException;
import org.braekpo1nt.mctmanager.config.exceptions.ConfigInvalidException;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.function.Function;
import java.util.logging.Level;

/**
 * Keeps the most recently loaded Preset in memory, so we don't need to perform file IO
 * for every call for information (e.g. tab completions)
 */
public class PresetStorageUtil {
    
    private final PresetController controller;
    
    public PresetStorageUtil(Main plugin) {
        this.controller = new PresetController(new File(plugin.getDataFolder(), "presets"));
    }
    
    /**
     * Save the preset to its file
     * @param preset the preset to save
     * @param presetFile the file to save the preset to
     * @throws ConfigIOException if there is an IO error saving the config to the file system
     */
    public void savePreset(@NotNull Preset preset, @NotNull String presetFile) throws ConfigIOException {
        controller.savePreset(preset, presetFile);
    }
    
    /**
     * Load the preset from its file
     * @return the loaded preset
     * @throws ConfigInvalidException if there is a problem parsing the JSON into a configDTO
     * @throws ConfigIOException if there is an IO problem getting the configDTO
     */
    public @NotNull Preset loadPreset(@NotNull String presetFile) throws ConfigException {
        return controller.getPreset(presetFile);
    }
    
    /**
     * Load preset from file, pass preset to modify method, save preset.
     * This is a convenience method for commands which interact with presets.
     * The modify method will be passed the loaded preset from the given preset file,
     * and then saved to the given file. If any errors occur along the way they
     * are caught and reported in the result. The return result of the
     * modify method will be returned if nothing goes wrong with loading or saving.
     * If the return value of the modify method is not successful, then the preset will 
     * not be saved.
     * @param presetFile the file to load and then save the preset from and to.
     * @param modify the method to modify the preset with
     * @return the result of the modification.
     */
    public CommandResult modifyPreset(@NotNull String presetFile, Function<Preset, CommandResult> modify) {
        Preset preset;
        try {
            preset = loadPreset(presetFile);
        } catch (ConfigException e) {
            Main.logger().log(Level.SEVERE, String.format("Could not load preset. %s", e.getMessage()), e);
            return CommandResult.failure(Component.empty()
                    .append(Component.text("Error occurred loading preset. See console for details: "))
                    .append(Component.text(e.getMessage())));
        }
        CommandResult result = modify.apply(preset);
        if (result instanceof FailureCommandResult) {
            return result;
        }
        try {
            savePreset(preset, presetFile);
        } catch (ConfigException e) {
            Main.logger().log(Level.SEVERE, String.format("Could not save preset. %s", e.getMessage()), e);
            return CommandResult.failure(Component.empty()
                    .append(Component.text("Error occurred saving preset. See console for details: "))
                    .append(Component.text(e.getMessage())));
        }
        return result;
    }
    
}
