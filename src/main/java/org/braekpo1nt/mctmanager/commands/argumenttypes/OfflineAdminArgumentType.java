package org.braekpo1nt.mctmanager.commands.argumenttypes;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import io.papermc.paper.command.brigadier.argument.resolvers.PlayerProfileListResolver;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class OfflineAdminArgumentType implements CustomArgumentType.Converted<OfflineAdminListResolver, PlayerProfileListResolver> {
    
    private final @NotNull GameManager gameManager;
    
    public OfflineAdminArgumentType(@NotNull GameManager gameManager) {
        this.gameManager = gameManager;
    }
    
    @Override
    public @NotNull OfflineAdminListResolver convert(@NotNull PlayerProfileListResolver profileResolver) throws CommandSyntaxException {
        return new OfflineAdminListResolver(gameManager, profileResolver);
    }
    
    @Override
    public <S> @NotNull CompletableFuture<Suggestions> listSuggestions(@NotNull CommandContext<S> context, @NotNull SuggestionsBuilder builder) {
        return gameManager.getAllAdminNames()
                .thenApply(names -> {
                    names.stream()
                            .filter(name -> name.toLowerCase().startsWith(builder.getRemainingLowerCase()))
                            .forEach(builder::suggest);
                    return builder.build();
                });
    }
    
    @Override
    public @NotNull ArgumentType<PlayerProfileListResolver> getNativeType() {
        return ArgumentTypes.playerProfiles();
    }
}
