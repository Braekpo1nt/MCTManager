package org.braekpo1nt.mctmanager.commands.mct.team.preset;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.braekpo1nt.mctmanager.Main;
import org.braekpo1nt.mctmanager.commands.argumenttypes.FileArgumentType;
import org.braekpo1nt.mctmanager.commands.argumenttypes.FileResolver;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.BrigadierSubCommand;
import org.braekpo1nt.mctmanager.commands.manager.brigadier.permissioned.Permissioned;
import org.braekpo1nt.mctmanager.games.gamemanager.GameManager;
import org.braekpo1nt.mctmanager.games.gamestate.preset.Preset;
import org.braekpo1nt.mctmanager.games.gamestate.preset.PresetStorageUtil;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class PresetCommand implements BrigadierSubCommand {
    
    public static final String PRESET_FILE_ARG = "presetFile";
    public static final String PRESET_MEMBER_IGN_ARG = "ign";
    private final @NotNull Main plugin;
    private final @NotNull GameManager gameManager;
    
    public PresetCommand(@NotNull Main plugin, @NotNull GameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
    }
    
    @Override
    public @NotNull Permissioned<CommandSourceStack> create() {
        PresetStorageUtil storageUtil = new PresetStorageUtil(plugin.getDataFolder());
        return Permissioned.literal("preset")
                .then(Permissioned.argument(PRESET_FILE_ARG,
                                new FileArgumentType(new File(plugin.getDataFolder(), "presets"), ".json"))
                        .then(new PresetApplySubCommand(plugin, gameManager, storageUtil).create())
                        .then(new PresetWhitelistSubCommand(storageUtil, plugin).create())
                        // editor:
                        .then(new PresetAddSubCommand(storageUtil).create())
                        .then(new PresetRemoveSubCommand(storageUtil).create())
                        .then(new PresetJoinSubCommand(plugin, storageUtil).create())
                        .then(new PresetLeaveSubCommand(storageUtil).create())
                        .then(new PresetAddMissingUUIDsCommand(plugin).create())
                )
                ;
    }
    
    public static CompletableFuture<Suggestions> suggestPresetTeams(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder, PresetStorageUtil storageUtil) {
        return CompletableFuture.supplyAsync(() -> {
            Preset preset;
            try {
                FileResolver resolver = ctx.getArgument(PresetCommand.PRESET_FILE_ARG, FileResolver.class);
                File presetFile = resolver.resolve();
                preset = storageUtil.loadPreset(presetFile);
            } catch (Exception e) {
                return builder.build();
            }
            preset.getTeams().stream()
                    .map(Preset.PresetTeam::getTeamId)
                    .filter(teamId -> teamId.toLowerCase().startsWith(builder.getRemainingLowerCase()))
                    .forEach(builder::suggest);
            return builder.build();
        });
    }
    
    /**
     * @param ctx the context
     * @param builder the suggestion builder
     * @param storageUtil the storage util to read the preset
     * @param plugin the plugin to get the offline players names from
     * @return a suggestion of all the offline and online players, and all the players in the given preset
     */
    public static CompletableFuture<Suggestions> suggestPresetCandidates(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder, PresetStorageUtil storageUtil, Main plugin) {
        return CompletableFuture.supplyAsync(() -> {
            Preset preset;
            try {
                FileResolver resolver = ctx.getArgument(PresetCommand.PRESET_FILE_ARG, FileResolver.class);
                File presetFile = resolver.resolve();
                preset = storageUtil.loadPreset(presetFile);
            } catch (Exception e) {
                plugin.getServer().getOnlinePlayers().stream()
                        .map(Player::getName)
                        .distinct()
                        .filter(name -> name.toLowerCase().startsWith(builder.getRemainingLowerCase()))
                        .forEach(builder::suggest);
                return builder.build();
            }
            Stream.concat(
                            plugin.getServer().getOnlinePlayers().stream()
                                    .map(Player::getName),
                            preset.getMembers().stream()
                                    .map(Preset.PresetParticipant::getIgn)
                    )
                    .distinct()
                    .filter(name -> name.toLowerCase().startsWith(builder.getRemainingLowerCase()))
                    .forEach(builder::suggest);
            return builder.build();
        });
    }
    
    /**
     * Searches the plugin's server for the offline player's UUID by their given IGN
     * @param ctx the context
     * @param builder the builder
     * @param plugin the plugin to search using
     * @return the UUID of the given IGN, according to {@link org.bukkit.Server#getOfflinePlayer(String)}, could be incorrect
     */
    public static CompletableFuture<Suggestions> suggestPresetUUIDs(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder, Main plugin) {
        return CompletableFuture.supplyAsync(() -> {
            String ign = ctx.getArgument(PRESET_MEMBER_IGN_ARG, String.class);
            UUID uuid = plugin.getServer().getPlayerUniqueId(ign);
            if (uuid != null) {
                builder.suggest(uuid.toString());
            }
            return builder.build();
        });
    }
    
    /**
     * @param ctx the context
     * @param builder the suggestion builder
     * @param storageUtil the storage util to read the preset
     * @return a suggestion list of all the players in the preset
     */
    public static CompletableFuture<Suggestions> suggestPresetParticipants(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder, PresetStorageUtil storageUtil) {
        return CompletableFuture.supplyAsync(() -> {
            Preset preset;
            try {
                FileResolver resolver = ctx.getArgument(PresetCommand.PRESET_FILE_ARG, FileResolver.class);
                File presetFile = resolver.resolve();
                preset = storageUtil.loadPreset(presetFile);
            } catch (Exception e) {
                return builder.build();
            }
            preset.getMembers().stream()
                    .map(Preset.PresetParticipant::getIgn)
                    .distinct()
                    .filter(name -> name.toLowerCase().startsWith(builder.getRemainingLowerCase()))
                    .forEach(builder::suggest);
            return builder.build();
        });
    }
}
