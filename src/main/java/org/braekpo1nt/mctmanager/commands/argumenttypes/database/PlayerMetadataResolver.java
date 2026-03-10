package org.braekpo1nt.mctmanager.commands.argumenttypes.database;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.resolvers.PlayerProfileListResolver;
import org.braekpo1nt.mctmanager.database.entities.PlayerMetadata;
import org.braekpo1nt.mctmanager.database.service.GameStateService;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.Collection;

public class PlayerMetadataResolver {
    private final @NotNull PlayerProfileListResolver profileResolver;
    private final @NotNull GameStateService gameStateService;
    
    public PlayerMetadataResolver(@NotNull PlayerProfileListResolver profileResolver, @NotNull GameStateService gameStateService) {
        this.profileResolver = profileResolver;
        this.gameStateService = gameStateService;
    }
    
    public Collection<PlayerMetadata> resolve(@NotNull CommandSourceStack ctx) throws CommandSyntaxException, SQLException {
        Collection<PlayerProfile> profiles = profileResolver.resolve(ctx);
        return gameStateService.getPlayerMetadatas(profiles.stream()
                .map(PlayerProfile::getId)
                .toList());
    }
}
