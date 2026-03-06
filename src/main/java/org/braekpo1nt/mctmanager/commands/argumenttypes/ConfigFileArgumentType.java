package org.braekpo1nt.mctmanager.commands.argumenttypes;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class ConfigFileArgumentType implements CustomArgumentType.Converted<String, String> {
    
    private final @NotNull GameManager gameManager;
    private final boolean activeOnly;
    
    public ConfigFileArgumentType(@NotNull GameManager gameManager, boolean activeOnly) {
        this.gameManager = gameManager;
        this.activeOnly = activeOnly;
    }
    
    @Override
    public @NotNull String convert(@NotNull String inputString) throws CommandSyntaxException {
        return inputString;
    }
    
    @Override
    public <S> @NotNull CompletableFuture<Suggestions> listSuggestions(@NotNull CommandContext<S> context, @NotNull SuggestionsBuilder builder) {
        return CompletableFuture.supplyAsync(() -> {
            context.getArgument();
            return builder.build();
        });
    }
    
    @Override
    public @NotNull ArgumentType<String> getNativeType() {
        return null;
    }
}
