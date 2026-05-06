package org.braekpo1nt.mctmanager.commands.argumenttypes.database;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import io.papermc.paper.command.brigadier.argument.resolvers.PlayerProfileListResolver;
import net.kyori.adventure.text.Component;
import org.braekpo1nt.mctmanager.database.entities.PlayerMetadata;
import org.braekpo1nt.mctmanager.database.service.GameStateService;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.Collection;
import java.util.UUID;

public class PlayerMetadataResolver {
    
    private static final SimpleCommandExceptionType ERROR_PLAYER_NOT_FOUND = new SimpleCommandExceptionType(MessageComponentSerializer.message().serialize(Component.empty()
            .append(Component.text("Could not find player"))
    ));
    
    private static final SimpleCommandExceptionType ERROR_MULTIPLE_PLAYERS_SELECTED = new SimpleCommandExceptionType(MessageComponentSerializer.message().serialize(Component.empty()
            .append(Component.text("Please select only one player"))
    ));
    
    private final @NotNull PlayerProfileListResolver profileResolver;
    private final @NotNull GameStateService gameStateService;
    
    public PlayerMetadataResolver(@NotNull PlayerProfileListResolver profileResolver, @NotNull GameStateService gameStateService) {
        this.profileResolver = profileResolver;
        this.gameStateService = gameStateService;
    }
    
    /**
     * @param ctx the context
     * @return a list of {@link PlayerMetadata} objects selected by the multiple players
     * @throws CommandSyntaxException if there is an input error
     * @throws SQLException if there is an issue with the database connection
     */
    public @NotNull Collection<PlayerMetadata> resolveMultiple(@NotNull CommandSourceStack ctx) throws CommandSyntaxException, SQLException {
        Collection<PlayerProfile> profiles = profileResolver.resolve(ctx);
        return gameStateService.getPlayerMetadatas(profiles.stream()
                .map(PlayerProfile::getId)
                .toList());
    }
    
    /**
     * @param ctx the context
     * @return a single {@link PlayerMetadata} object referencing the input player
     * @throws CommandSyntaxException if the player can't be found or if a target selector chooses multiple players
     * @throws SQLException if there is a problem communicating with the database
     */
    public @NotNull PlayerMetadata resolveSingle(@NotNull CommandSourceStack ctx) throws CommandSyntaxException, SQLException {
        Collection<PlayerProfile> profiles = profileResolver.resolve(ctx);
        if (profiles.isEmpty()) {
            throw ERROR_PLAYER_NOT_FOUND.create();
        }
        if (profiles.size() != 1) {
            throw ERROR_MULTIPLE_PLAYERS_SELECTED.create();
        }
        PlayerProfile playerProfile = profiles.stream().findFirst().get();
        UUID id = playerProfile.getId();
        if (id == null) {
            throw ERROR_PLAYER_NOT_FOUND.create();
        }
        PlayerMetadata playerMetadata = gameStateService.getPlayerMetadata(id);
        if (playerMetadata == null) {
            throw ERROR_PLAYER_NOT_FOUND.create();
        }
        return playerMetadata;
    }
}
