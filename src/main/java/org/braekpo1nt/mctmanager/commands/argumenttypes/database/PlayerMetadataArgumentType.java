package org.braekpo1nt.mctmanager.commands.argumenttypes.database;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import io.papermc.paper.command.brigadier.argument.resolvers.PlayerProfileListResolver;
import org.braekpo1nt.mctmanager.database.service.GameStateService;
import org.jetbrains.annotations.NotNull;

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
    public @NotNull ArgumentType<PlayerProfileListResolver> getNativeType() {
        return ArgumentTypes.playerProfiles();
    }
}
