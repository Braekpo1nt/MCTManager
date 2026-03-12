package org.braekpo1nt.mctmanager.commands.mct.team.preset;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.argumenttypes.FileResolver;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierAdapters;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierSubCommand;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.permissioned.Permissioned;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CommandResult;
import org.braekpo1nt.mctmanager.commands.manager.commandresult.CompositeCommandResult;
import org.braekpo1nt.mctmanager.database.entities.AllPlayersEntity;
import org.braekpo1nt.mctmanager.database.entities.PlayerMetadata;
import org.braekpo1nt.mctmanager.database.service.GameStateService;
import org.braekpo1nt.mctmanager.games.gamestate.preset.Preset;
import org.braekpo1nt.mctmanager.games.gamestate.preset.PresetStorageUtil;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PresetResolveCommand implements BrigadierSubCommand {
    
    private final @NotNull Main plugin;
    private final @NotNull PresetStorageUtil storageUtil;
    private final @NotNull GameStateService gameStateService;
    
    public PresetResolveCommand(@NotNull Main plugin, @NotNull PresetStorageUtil storageUtil, @NotNull GameStateService gameStateService) {
        this.plugin = plugin;
        this.storageUtil = storageUtil;
        this.gameStateService = gameStateService;
    }
    
    @Override
    public @NotNull Permissioned<CommandSourceStack> create() {
        return Permissioned.literal("resolve")
                .executes(BrigadierAdapters.wraps(this::executeResolve))
                ;
    }
    
    /**
     * Takes all the player IGNs in the preset and adds them to the all_players and player_metadata database tables
     * @param ctx the context
     * @return the result
     */
    private @NotNull CommandResult executeResolve(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        FileResolver resolver = ctx.getArgument(PresetCommand.PRESET_FILE_ARG, FileResolver.class);
        File presetFile = resolver.resolve();
        return CommandResult.async(plugin,
                Component.empty()
                        .append(Component.text("Resolving preset file "))
                        .append(Component.text(presetFile.getName())
                                .decorate(TextDecoration.BOLD))
                ,
                () -> {
                    Preset preset = storageUtil.loadPreset(presetFile);
                    List<CommandResult> results = new ArrayList<>();
                    List<AllPlayersEntity> allPlayersEntities = new ArrayList<>();
                    List<PlayerMetadata> playerMetadatas = new ArrayList<>();
                    for (String ign : preset.getMembers()) {
                        try {
                            AllPlayersEntity player = gameStateService.getPlayer(ign);
                            if (player == null) {
                                OfflinePlayer offlinePlayer = plugin.getServer().getOfflinePlayer(ign);
                                String uuidStr = offlinePlayer.getUniqueId().toString();
                                allPlayersEntities.add(AllPlayersEntity.builder()
                                        .uuid(uuidStr)
                                        .ign(ign)
                                        .firstSeenAt(new Date())
                                        .build());
                                playerMetadatas.add(PlayerMetadata.builder()
                                        .participantUUID(uuidStr)
                                        .ign(ign)
                                        .discordUsername(null)
                                        .currentTokens(0)
                                        .lifetimeTokens(0)
                                        .percentRank(0.0)
                                        .build());
                                results.add(CommandResult.success(Component.empty()
                                        .append(Component.text("New player: "))
                                        .append(Component.text(ign))
                                        .append(Component.text(" - "))
                                        .append(Component.text(uuidStr))
                                ));
                            }
                        } catch (SQLException e) {
                            return CommandResult.sqlException("find a player by name while resolve the preset file", e);
                        } catch (GameStateService.MultiplePlayersWithNameException e) {
                            results.add(CommandResult.success(Component.empty()
                                    .append(Component.text("Warning: multiple players with the name "))
                                    .append(Component.text(ign)
                                            .decorate(TextDecoration.BOLD))
                                    .append(Component.text(" exist in the all_players database"))
                                    .color(NamedTextColor.YELLOW)));
                        }
                    }
                    try {
                        gameStateService.registerParticipantsIfNotRegistered(allPlayersEntities, playerMetadatas);
                    } catch (SQLException e) {
                        return CommandResult.sqlException("commit resolved players to the database", e);
                    }
                    results.add(CommandResult.success(Component.text("Finished resolving preset file")));
                    return CompositeCommandResult.all(results);
                });
    }
}
