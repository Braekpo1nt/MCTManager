package org.braekpo1nt.mctmanager.commands;

import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;

public class CommandUtils {
    
    private static final Map<String, List<String>> GAME_CONFIGS = new HashMap<>();
    private static @NotNull List<String> PRESET_FILES = Collections.emptyList();
    
    /**
     * @param value the string to check if it is an integer
     * @return true if the string is an integer, false if not
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isInteger(@NotNull String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * @param value the string to check if it is a double
     * @return true if the string is a double, false if not
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isDouble(@NotNull String value) {
        try {
            Double.parseDouble(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * @param value the string to check if it is a float
     * @return true if the string is a float, false if not
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isFloat(@NotNull String value) {
        try {
            Float.parseFloat(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * @param value the string to be parsed
     * @return the boolean value the string represents if the string can be successfully parsed to a boolean, null if
     * the string couldn't be parsed to a boolean
     */
    public static @Nullable Boolean toBoolean(@NotNull String value) {
        String lowerCase = value.toLowerCase();
        switch (lowerCase) {
            case "true", "yes", "t", "y", "1" -> {
                return true;
            }
            case "false", "no", "f", "n", "0" -> {
                return false;
            }
            default -> {
                return null;
            }
        }
    }
    
    public static @NotNull List<String> partialMatchTabList(@NotNull Collection<@NotNull String> list, @Nullable String partial) {
        if (partial == null || partial.isEmpty()) {
            return list.stream().toList();
        }
        String lowerCasePartial = partial.toLowerCase();
        return list.stream().filter(s -> s.toLowerCase().startsWith(lowerCasePartial)).toList();
    }
    
    /**
     * List the config files in the directory for the given gameID
     * @param gameID the gameID to get the configs for
     * @return the configs associated with that gameID, or an empty list
     * if none are found
     */
    public static @NotNull List<String> getGameConfigs(@NotNull String gameID) {
        return GAME_CONFIGS.getOrDefault(gameID, Collections.emptyList());
    }
    
    /**
     * Searches the plugin's data folder asynchronously to store
     * references to each game's config folder and the json config
     * files contained within, enabling cheap tab completion.
     * @param plugin enables asynchronous file IO
     */
    public static void refreshGameConfigs(Main plugin) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            for (String gameID : GameType.GAME_IDS.keySet()) {
                File configDir = new File(plugin.getDataFolder(), gameID);
                if (configDir.isDirectory()) {
                    File[] jsonFiles = configDir.listFiles(file ->
                            file.isFile() && file.getName().endsWith(".json"));
                    if (jsonFiles != null) {
                        GAME_CONFIGS.put(gameID, Arrays.stream(jsonFiles)
                                .map(File::getName)
                                .toList());
                    }
                }
            }
        });
    }
    
    public static @NotNull List<String> getPresetFiles() {
        return PRESET_FILES;
    }
    
    public static void refreshPresetFiles(Main plugin) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            File configDir = new File(plugin.getDataFolder(), "presets");
            if (configDir.isDirectory()) {
                File[] jsonFiles = configDir.listFiles(file ->
                        file.isFile() && file.getName().endsWith(".json"));
                if (jsonFiles != null) {
                    PRESET_FILES = Arrays.stream(jsonFiles)
                            .map(File::getName)
                            .toList();
                }
            }
        });
    }
    
    public static @NotNull String[] removeElement(@NotNull String[] original, int indexToRemove) {
        if (indexToRemove < 0 || indexToRemove >= original.length) {
            throw new IllegalArgumentException("Invalid index");
        }
        
        String[] result = new String[original.length - 1];
        System.arraycopy(original, 0, result, 0, indexToRemove);
        System.arraycopy(original, indexToRemove + 1, result, indexToRemove, original.length - indexToRemove - 1);
        return result;
    }
}
