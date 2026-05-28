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
