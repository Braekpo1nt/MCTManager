package org.braekpo1nt.mctmanager.commands.argumenttypes;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class OfflineParticipantArgumentType implements CustomArgumentType.Converted<OfflineParticipantResolver, String> {
    
    private final @NotNull GameManager gameManager;
    
    /**
     * @param gameManager the gameManager used to search through and retrieve participants
     */
    public OfflineParticipantArgumentType(@NotNull GameManager gameManager) {
        this.gameManager = gameManager;
    }
    
    @Override
    public @NotNull OfflineParticipantResolver convert(@NotNull String ign) throws CommandSyntaxException {
        return new OfflineParticipantResolver(gameManager, ign);
    }
    
    @Override
    public <S> @NotNull CompletableFuture<Suggestions> listSuggestions(@NotNull CommandContext<S> context, @NotNull SuggestionsBuilder builder) {
        return CompletableFuture.supplyAsync(() -> {
            gameManager.getAllParticipantNames().stream()
                    .filter(name -> name.toLowerCase().startsWith(builder.getRemainingLowerCase()))
                    .forEach(builder::suggest);
            return builder.build();
        });
    }
    
    @Override
    public @NotNull ArgumentType<String> getNativeType() {
        return StringArgumentType.word();
    }
}
