package org.braekpo1nt.mctmanager.commands.argumenttypes.database;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import io.papermc.paper.command.brigadier.argument.resolvers.PlayerProfileListResolver;
import org.braekpo1nt.mctmanager.database.service.GameStateService;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class PlayerMetadataArgumentType implements CustomArgumentType.Converted<PlayerMetadataResolver, PlayerProfileListResolver> {
    
    
    private final @NotNull GameStateService gameStateService;
    
    public PlayerMetadataArgumentType(@NotNull GameStateService gameStateService) {
        this.gameStateService = gameStateService;
    }
    
    @Override
    public @NotNull PlayerMetadataResolver convert(@NotNull PlayerProfileListResolver nativeType) throws CommandSyntaxException {
        return new PlayerMetadataResolver(nativeType, gameStateService);
    }
    
    @Override
    public <S> @NotNull CompletableFuture<Suggestions> listSuggestions(@NotNull CommandContext<S> ctx, @NotNull SuggestionsBuilder builder) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<String> igns = gameStateService.getPlayerIGNsPartialMatch(builder.getRemaining());
                igns.forEach(builder::suggest);
            } catch (SQLException e) {
                return builder.build();
            }
            
            return builder.build();
        });
    }
    
    @Override
    public @NotNull ArgumentType<PlayerProfileListResolver> getNativeType() {
        return ArgumentTypes.playerProfiles();
    }
}
