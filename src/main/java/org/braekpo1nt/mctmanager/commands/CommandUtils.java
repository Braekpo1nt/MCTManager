package org.braekpo1nt.mctmanager.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.CommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.utils.ColorMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class CommandUtils {
    
    /**
     * @deprecated remove this, no longer used
     */
    @Deprecated
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
     * @deprecated use {@link #getGameConfigs(Main, GameType)}
     */
    @Deprecated
    public static @NotNull List<String> getGameConfigs(@NotNull String gameID) {
        return GAME_CONFIGS.getOrDefault(gameID, Collections.emptyList());
    }
    
    /**
     * Searches the plugin's data folder asynchronously to store
     * references to each game's config folder and the json config
     * files contained within, enabling cheap tab completion.
     * @param plugin enables asynchronous file IO
     * @deprecated use {@link #getGameConfigs(Main, GameType)}
     */
    @Deprecated
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
    
    public static @NotNull List<String> getGameConfigs(@NotNull Main plugin, @NotNull GameType gameId) {
        File configDir = new File(plugin.getDataFolder(), gameId.getId());
        if (configDir.isDirectory()) {
            File[] jsonFiles = configDir.listFiles(file ->
                    file.isFile() && file.getName().endsWith(".json"));
            if (jsonFiles != null) {
                return Arrays.stream(jsonFiles)
                        .map(File::getName)
                        .toList();
            }
        }
        return Collections.emptyList();
    }
    
    public static CompletableFuture<Suggestions> suggestConfigFiles(@NotNull Main plugin, CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        return CompletableFuture.supplyAsync(() -> {
            GameType gameId = ctx.getArgument("gameId", GameType.class);
            getGameConfigs(plugin, gameId).stream()
                    .filter(configFile -> configFile.startsWith(builder.getRemaining()))
                    .forEach(builder::suggest);
            return builder.build();
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
    //========================
    // a few helpers for theoretical automatic permission node creation:
    
    //========================
    
    public static @NotNull Component displayCommandNodes(List<List<String>> allPermNodes) {
        TextComponent.Builder builder = Component.text();
        for (List<String> permNodes : allPermNodes) {
            for (String permNode : permNodes) {
                builder
                        .append(Component.text(permNode))
                        .append(Component.text("."));
            }
            builder.append(Component.newline());
        }
        return builder.build();
    }
    
    public static List<List<String>> toPermNodes(CommandNode<CommandSourceStack> root) {
        List<List<String>> result = new ArrayList<>();
        Deque<String> path = new ArrayDeque<>();
        
        traverse(root, path, result);
        
        return result;
    }
    
    private static void traverse(
            CommandNode<CommandSourceStack> node,
            Deque<String> path,
            List<List<String>> result
    ) {
        // Add current node to path
        path.addLast(node.getName());
        
        // Record current path
        result.add(new ArrayList<>(path));
        
        // Traverse children
        for (CommandNode<CommandSourceStack> child : node.getChildren()) {
            traverse(child, path, result);
        }
        
        // Backtrack
        path.removeLast();
    }
    
    public static CompletableFuture<Suggestions> suggestColor(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        List<String> suggestions = ColorMap.getPartiallyMatchingColorStrings(builder.getRemainingLowerCase());
        for (String suggestion : suggestions) {
            builder.suggest(suggestion);
        }
        return builder.buildFuture();
    }
    
    /**
     * @param remainingValues a string containing space-separated partially/fully completed
     * string of values (such as teamIds)
     * (e.g. "purple red oran" or "purple red orange")
     * @param validValues the list of valid values to suggest from
     * (e.g. ["purple", "red", "orange", "yellow"])
     * @return a list of suggestions for brigadier commands which include all fully
     * completed valid entries, and the partially complete last entry and all
     * possible partial matches.
     */
    public static List<String> suggestGreedyList(String remainingValues, Collection<String> validValues) {
        // ["red", "purple", "ye"] for `/...red purple ye`
        // ["red", "purple", "yell"] for `/...red purple yell`
        List<String> alreadyTyped = Arrays.stream(remainingValues.split("\\s+"))
                .filter(s -> !s.isBlank())
                .collect(Collectors.toCollection(ArrayList::new));
        if (remainingValues.endsWith(" ")) {
            alreadyTyped.add("");
        }
        List<String> result = new ArrayList<>();
        if (!alreadyTyped.isEmpty()) {
            // "yell" for `/...red purple yell`
            String trueRemaining = alreadyTyped.getLast();
            // instead of the remainingValues, remove the partially typed value and add back the fully typed one as a suggestion
            String alreadyFullyTyped = remainingValues.substring(0, remainingValues.length() - trueRemaining.length());
            // if trueRemaining is a partial value
            validValues.stream()
                    .filter(value -> !alreadyTyped.contains(value))
                    .filter(value -> value.startsWith(trueRemaining))
                    .forEach(value -> result.add(alreadyFullyTyped + value));
            if (validValues.contains(trueRemaining)) {
                // instead of adding just trueRemaining, suggest the whole string as valid
                result.add(remainingValues);
            }
        } else {
            validValues
                    .forEach(value -> result.add(remainingValues + value));
        }
        return result;
    }
    
    public static Component copiable(String text) {
        return Component.text(text)
                .decorate(TextDecoration.UNDERLINED)
                .clickEvent(ClickEvent.copyToClipboard(text))
                .hoverEvent(HoverEvent.showText(Component.text("Copy")))
                ;
    }
}
