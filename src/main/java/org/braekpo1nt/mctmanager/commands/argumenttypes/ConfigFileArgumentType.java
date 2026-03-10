package org.braekpo1nt.mctmanager.commands.argumenttypes;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import org.braekpo1nt.mctmanager.games.game.enums.GameType;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Expects there to be a gameId argument before this. Used to find
 * config files associated with the gameId.
 */
public class ConfigFileArgumentType implements CustomArgumentType.Converted<String, String> {
    
    private final @NotNull GameManager gameManager;
    private final boolean activeOnly;
    private final @NotNull String gameIdArgumentName;
    
    public ConfigFileArgumentType(@NotNull GameManager gameManager, boolean activeOnly, @NotNull String gameIdArgumentName) {
        this.gameManager = gameManager;
        this.activeOnly = activeOnly;
        this.gameIdArgumentName = gameIdArgumentName;
    }
    
    @Override
    public @NotNull String convert(@NotNull String inputString) throws CommandSyntaxException {
        return inputString;
    }
    
    @Override
    public <S> @NotNull CompletableFuture<Suggestions> listSuggestions(@NotNull CommandContext<S> context, @NotNull SuggestionsBuilder builder) {
        return CompletableFuture.supplyAsync(() -> {
            GameType gameId;
            try {
                gameId = context.getArgument(gameIdArgumentName, GameType.class);
            } catch (Exception e) {
                return builder.build();
            }
            List<String> configFiles;
            if (activeOnly) {
                configFiles = gameManager.getActiveConfigFiles(gameId);
            } else {
                configFiles = gameManager.getConfigFiles(gameId);
            }
            configFiles.stream()
                    .filter(configFile -> configFile.startsWith(builder.getRemaining()))
                    .forEach(builder::suggest);
            return builder.build();
        });
    }
    
    @Override
    public @NotNull ArgumentType<String> getNativeType() {
        return StringArgumentType.word();
    }
}
